package se.sdmapeg.server.workers;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sdmapeg.common.communication.Connection;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.common.tasks.SimpleFailure;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.server.communication.ConnectionAcceptor;
import se.sdmapeg.server.communication.ConnectionAcceptorCallback;
import se.sdmapeg.server.communication.ConnectionHandler;
import se.sdmapeg.serverworker.TaskId;
import se.sdmapeg.serverworker.communication.ServerToWorkerMessage;
import se.sdmapeg.serverworker.communication.WorkerToServerMessage;

/**
 *
 * @author niclas
 */
public final class WorkerCoordinatorImpl implements WorkerCoordinator {
	private static final Logger LOG = LoggerFactory.getLogger(WorkerCoordinatorImpl.class);
	private final WorkerCoordinatorListenerSupport listeners;
	private final ExecutorService connectionThreadPool;
	private final ConnectionHandler<ServerToWorkerMessage,
			WorkerToServerMessage> connectionHandler;
	private final WorkerCoordinatorCallback callback;
	private final Map<TaskId, Task<?>> taskMap = new ConcurrentHashMap<>();
	private final Map<TaskId, Worker> taskAssignmentMap =
		new ConcurrentHashMap<>();
	private final ConcurrentMap<InetAddress, Worker> addressMap =
		new ConcurrentHashMap<>();
	private final AtomicBoolean started = new AtomicBoolean(false);

	private WorkerCoordinatorImpl(ExecutorService connectionThreadPool,
			ConnectionHandler<ServerToWorkerMessage,
					WorkerToServerMessage> connectionHandler,
			WorkerCoordinatorCallback callback,
			Executor listenerExecutor) {
		this.connectionThreadPool = connectionThreadPool;
		this.connectionHandler = connectionHandler;
		this.callback = callback;
		this.listeners = WorkerCoordinatorListenerSupport.newListenerSupport(
				listenerExecutor);
	}

	@Override
	public void handleTask(TaskId taskId, Task<?> task) {
		taskMap.put(taskId, task);
		Worker worker;
		/*
		 * Keep trying until the task has been successfully assigned. This
		 * should normally succeed on the first attempt.
		 */
		do {
			worker = leastLoadedAvailableWorker();
			if (worker == null) {
				taskMap.remove(taskId);
				taskAssignmentMap.remove(taskId);
				callback.handleResult(taskId, SimpleFailure.newSimpleFailure(
						new ExecutionException("No workers available.", null)));
				return;
			}
			taskAssignmentMap.put(taskId, worker);
		} while (!worker.assignTask(taskId, task));
		LOG.info("Task {} sent to {}", taskId, worker);
		listeners.taskAssigned(taskId, worker.getAddress());
	}

	@Override
	public void cancelTask(TaskId taskId) {
		taskMap.remove(taskId);
		Worker worker = taskAssignmentMap.remove(taskId);
		worker.cancelTask(taskId);
		listeners.taskAborted(taskId, worker.getAddress());
	}

	@Override
	public void shutDown() {
		try {
			/*
			 * Closes the connection handler. This will be noticed by the
			 * connection acceptor thread which will handle the rest of the
			 * shutdown work.
			 */
			connectionHandler.close();
		} catch (IOException ex) {
			LOG.warn("An error occurred while closing the connection handler",
					 ex);
		}
	}

	@Override
	public void disconnectWorker(InetAddress workerAddress) {
		Worker worker = addressMap.get(workerAddress);
		if (worker == null) {
			return;
		}
		disconnectWorker(worker);
	}

	@Override
	public void start() {
		/*
		 * compareAndSet to ensure that the connection acceptor thread will only
		 * be started the first time this method is called.
		 */
		if (started.compareAndSet(false, true)) {
			// Start a new thread to deal with incoming connections
			ConnectionAcceptor.acceptConnections(connectionThreadPool,
				connectionHandler, new WorkerConnectionCallback());
			LOG.info("Worker Coordinator Started.");
		}
	}

	@Override
	public WorkerCoordinatorState getState() {
		if (isStopped()) {
			return WorkerCoordinatorState.STOPPED;
		} else if (isStaretd()) {
			return WorkerCoordinatorState.STARTED;
		} else {
			return WorkerCoordinatorState.CREATED;
		}
	}

	private boolean isStopped() {
		return !connectionHandler.isOpen();
	}

	private boolean isStaretd() {
		return started.get();
	}

	private Worker leastLoadedAvailableWorker() {
		List<Worker> workers = new ArrayList<>(addressMap.values());
		int leastLoad = Integer.MAX_VALUE;
		Worker selected = null;
		for (Worker candidate : workers) {
			if (candidate.isAcceptingWork()) {
				int load = candidate.getLoad();
				if (leastLoad > load) {
					selected = candidate;
					leastLoad = load;
				}
			}
		}
		return selected;
	}

	private void stealTasks(int desired) {
		List<WorkerLoadSnapshot> snapshots = createLoadSnapshotList();
		Collections.sort(snapshots, WorkerLoadSnapshot.descendingComparator());
		int stolen = 0;
		for (WorkerLoadSnapshot snapshot : snapshots) {
			Worker worker = snapshot.getWorker();
			stolen += stealTasks(worker, desired - stolen);
			if (stolen == desired) {
				break;
			}
		}
	}

	private int stealTasks(Worker worker, int desired) {
		int load = worker.getLoad();
		if (load <= 1) {
			return 0;
		}
		int tasksToSteal = Math.min(load - 1, desired);
		worker.stealTasks(tasksToSteal);
		return tasksToSteal;
	}

	private void reassignTask(TaskId taskId) {
		taskAssignmentMap.remove(taskId);
		Task<?> task = taskMap.remove(taskId);
		handleTask(taskId, task);
	}

	private List<WorkerLoadSnapshot> createLoadSnapshotList() {
		/*
		 * Since new workers may be added at any time, we give the list a
		 * slightly larger initial capacity.
		 */
		List<WorkerLoadSnapshot> snapshots = new ArrayList<>(addressMap.size() + 5);
		for (Worker worker : addressMap.values()) {
			snapshots.add(WorkerLoadSnapshot.newSnapshot(worker));
		}
		return snapshots;
	}

	private void disconnectWorker(Worker worker) {
		/*
		 * Disconnects the worker. This will be noticed by the worker listener
		 * thread, which will handle the rest of the cleanup work.
		 */
		worker.disconnect();
	}

	@Override
	public void addListener(WorkerCoordinatorListener listener) {
		listeners.addListener(listener);
	}

	@Override
	public void removeListener(WorkerCoordinatorListener listener) {
		listeners.removeListener(listener);
	}

	/**
	 * Creates a new WorkerCoordinator with the specified connectionThreadPool,
	 * connectionHandler, callback, and listener executor.
	 *
	 * @param connectionThreadPool a thread pool for handling connections
	 * @param connectionHandler a connection handler for dealing with new
	 *							connections
	 * @param callback a callback to be notified of events
	 * @param listenerExecutor an Executor to be used for notifying listeners
	 * @return the created ClientManager
	 */
	public static WorkerCoordinator newWorkerCoordinator(
			ExecutorService connectionThreadPool,
			ConnectionHandler<ServerToWorkerMessage, WorkerToServerMessage>
				connectionHandler,
			WorkerCoordinatorCallback callback,
			Executor listenerExecutor) {
		return new WorkerCoordinatorImpl(connectionThreadPool,
				connectionHandler, callback, listenerExecutor);
	}

	private final class WorkerConnectionCallback
		implements ConnectionAcceptorCallback<ServerToWorkerMessage,
			WorkerToServerMessage> {

		@Override
		public void connectionReceived(Connection<ServerToWorkerMessage,
				WorkerToServerMessage> connection) {
			Worker worker = WorkerImpl.newWorker(connection);
			if (addressMap.put(worker.getAddress(), worker) == null) {
				LOG.info("{} connected", worker);
				listeners.workerConnected(worker.getAddress());
				// Start a new thread to listen to the worker
				connectionThreadPool.submit(new WorkerListener(worker));
			} else {
				LOG.warn("Connection refused: {} attempted to connect, but was"
						+ " already connected", worker);
				worker.disconnect();
			}
		}

		@Override
		public void connectionHandlerClosed() {
			/*
			 * Disconnect all currently connected workers. Since this method is
			 * called by the only thread responsible for accepting new
			 * connections, we can safely assume that the collection will
			 * remain up to date without having to worry about new workers being
			 * added concurrently.
			 */
			for (Worker worker : addressMap.values()) {
				disconnectWorker(worker);
			}
			LOG.info("Worker Coordinator Stopped");
		}
	}

	private final class WorkerListener implements Runnable {
		private final Worker worker;

		public WorkerListener(Worker worker) {
			this.worker = worker;
		}

		@Override
		public void run() {
			worker.listen(new WorkerEventCallback(worker));
		}
	}

	private final class WorkerEventCallback implements WorkerCallback {
		private final Worker worker;

		public WorkerEventCallback(Worker worker) {
			this.worker = worker;
		}

		@Override
		public void taskCompleted(TaskId taskId, Result<?> result) {
			taskAssignmentMap.remove(taskId);
			taskMap.remove(taskId);
			LOG.info("Task {} completed by {}", taskId, worker);
			listeners.resultReceived(taskId, worker.getAddress());
			callback.handleResult(taskId, result);
		}

		@Override
		public void taskStolen(TaskId taskId) {
			LOG.info("Task {} stolen from {}", taskId, worker);
			listeners.taskAborted(taskId, worker.getAddress());
			reassignTask(taskId);
		}

		@Override
		public void workerDisconnected() {
			addressMap.remove(worker.getAddress());
			listeners.workerDisconnected(worker.getAddress());
			/*
			 * Reassign all active tasks of this worker. Since this method is
			 * called from the only thread responsible for completing tasks
			 * assigned to this worker, and a disconnected worker will never
			 * accept any new tasks, we can safely assume that the set of active
			 * tasks will be up to date and that no tasks will be added or
			 * removed concurrently.
			 */
			for (TaskId taskId : worker.getActiveTasks()) {
				LOG.info("Reassigning task {} from {}", taskId, worker);
				reassignTask(taskId);
			}
		}

		@Override
		public void workRequested() {
			stealTasks(worker.getParallellWorkCapacity());
			LOG.info("Work requested by {}", worker);
		}
	}
}
