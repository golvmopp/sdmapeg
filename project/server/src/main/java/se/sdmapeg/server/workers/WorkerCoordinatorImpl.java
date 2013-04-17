package se.sdmapeg.server.workers;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sdmapeg.common.communication.Connection;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.common.tasks.SimpleFailure;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.server.communication.ConnectionAcceptor;
import se.sdmapeg.server.communication.ConnectionHandler;
import se.sdmapeg.serverworker.TaskId;
import se.sdmapeg.serverworker.communication.ServerToWorkerMessage;
import se.sdmapeg.serverworker.communication.WorkerToServerMessage;

/**
 *
 * @author niclas
 */
public class WorkerCoordinatorImpl implements WorkerCoordinator {
	private static final Logger LOG = LoggerFactory.getLogger(WorkerCoordinatorImpl.class);
	private final ExecutorService connectionThreadPool;
	private final ConnectionHandler<ServerToWorkerMessage,
			WorkerToServerMessage> connectionHandler;
	private final WorkerCoordinatorCallback callback;
	private final Worker.Callback workerCallback = new WorkerCallback();
	private final Map<TaskId, Task<?>> taskMap = new ConcurrentHashMap<>();
	private final Map<TaskId, Worker> taskAssignmentMap =
		new ConcurrentHashMap<>();
	private final Map<InetAddress, Worker> addressMap =
		new ConcurrentHashMap<>();
	private final AtomicBoolean started = new AtomicBoolean(false);

	public WorkerCoordinatorImpl(ExecutorService connectionThreadPool,
			ConnectionHandler<ServerToWorkerMessage,
					WorkerToServerMessage> connectionHandler,
			WorkerCoordinatorCallback callback) {
		this.connectionThreadPool = connectionThreadPool;
		this.connectionHandler = connectionHandler;
		this.callback = callback;
	}

	@Override
	public void handleTask(TaskId taskId, Task<?> task) {
		taskMap.put(taskId, task);
		Worker worker;
		// Keep trying until the task has been successfully assigned.
		// This should normally succeed on the first attempt.
		do {
			worker = leastLoadedAvailableWorker();
			if (worker == null) {
				taskMap.remove(taskId);
				callback.handleResult(taskId, new SimpleFailure<>(
						new ExecutionException("No workers available.", null)));
				return;
			}
		} while (!worker.assignTask(taskId, task));
		taskAssignmentMap.put(taskId, worker);
	}

	@Override
	public void cancelTask(TaskId taskId) {
		taskMap.remove(taskId);
		Worker worker = taskAssignmentMap.remove(taskId);
		worker.cancelTask(taskId);
	}

	@Override
	public void shutDown() {
		try {
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
		worker.disconnect();
	}

	@Override
	public void start() {
		if (started.compareAndSet(false, true)) {
			connectionThreadPool.submit(new ConnectionAcceptor(
					connectionHandler, new ConnectionAcceptorCallback()));
		}
	}

	@Override
	public WorkerCoordinator.State getState() {
		if (isStopped()) {
			return WorkerCoordinator.State.STOPPED;
		} else if (isStaretd()) {
			return WorkerCoordinator.State.STARTED;
		} else {
			return WorkerCoordinator.State.CREATED;
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
		List<LoadSnapshot> snapshots = createLoadSnapshotList();
		Collections.sort(snapshots, LoadSnapshot.descendingComparator());
		int stolen = 0;
		for (LoadSnapshot snapshot : snapshots) {
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

	private void connectionHandlerClosed() {
		for (Worker worker : addressMap.values()) {
			worker.disconnect();
		}
	}

	private void reassignTask(TaskId taskId) {
		taskAssignmentMap.remove(taskId);
		Task<?> task = taskMap.remove(taskId);
		handleTask(taskId, task);
	}

	private List<LoadSnapshot> createLoadSnapshotList() {
		// Since new workers may be added at any time, we give the list a
		// slightly larger initial capacity
		List<LoadSnapshot> snapshots = new ArrayList<>(addressMap.size() + 5);
		for (Worker worker : addressMap.values()) {
			snapshots.add(new LoadSnapshot(worker));
		}
		return snapshots;
	}

	/**
	 * Immutable class used for sorting (mutable) workers according to their
	 * load at some point in time. Since Workers are mutable and may be modified
	 * by other threads at any point in time, attempting to sort them using a
	 * regular comparator might lead to unspecified behaviour in the sorting
	 * algorithm if another thread changes them during the sort.
	 * <p />
	 * This class provides an immutable snapshot of the state of the Worker,
	 * which can safely be sorted without risk of other threads modifying it in
	 * the process. Due to this immutable nature, it is possible that a list of
	 * LoadSnapshots will be outdated after being sorted. This class is intended
	 * for use in situations where the risk of having an outdated list is an
	 * acceptable tradeoff.
	 */
	private static final class LoadSnapshot {
		private static final Comparator<LoadSnapshot> ASCENDING =
			new LoadComparator();
		private static final Comparator<LoadSnapshot> DESCENDING =
			Collections.reverseOrder(ASCENDING);
		private final Worker worker;
		private final int load;

		public LoadSnapshot(Worker worker) {
			this.worker = worker;
			this.load = worker.getLoad();
		}

		public Worker getWorker() {
			return worker;
		}

		public static Comparator<LoadSnapshot> ascendingComparator() {
			return ASCENDING;
		}

		public static Comparator<LoadSnapshot> descendingComparator() {
			return DESCENDING;
		}

		private static final class LoadComparator
				implements Comparator<LoadSnapshot> {
			@Override
			public int compare(LoadSnapshot o1, LoadSnapshot o2) {
				return Integer.compare(o1.load, o2.load);
			}			
		}
	}

	private final class ConnectionAcceptorCallback
		implements ConnectionAcceptor.Callback<ServerToWorkerMessage,
			WorkerToServerMessage> {

		@Override
		public void connectionReceived(Connection<ServerToWorkerMessage,
				WorkerToServerMessage> connection) {
			Worker worker = new WorkerImpl(connection, workerCallback);
			addressMap.put(worker.getAddress(), worker);
			connectionThreadPool.submit(new WorkerListener(worker));
		}

		@Override
		public void connectionHandlerClosed() {
			WorkerCoordinatorImpl.this.connectionHandlerClosed();
		}
	}

	private final class WorkerListener implements Runnable {
		private final Worker worker;

		public WorkerListener(Worker worker) {
			this.worker = worker;
		}

		@Override
		public void run() {
			worker.listen();
		}
	}

	private final class WorkerCallback implements Worker.Callback {

		@Override
		public void taskCompleted(Worker worker, TaskId taskId,
				Result<?> result) {
			taskAssignmentMap.remove(taskId);
			taskMap.remove(taskId);
			callback.handleResult(taskId, result);
		}

		@Override
		public void taskStolen(Worker worker, TaskId taskId) {
			reassignTask(taskId);
		}

		@Override
		public void workerDisconnected(Worker worker) {
			addressMap.remove(worker.getAddress());
			for (TaskId taskId : worker.getActiveTasks()) {
				reassignTask(taskId);
			}
		}

		@Override
		public void workRequested(Worker worker) {
			stealTasks(worker.getParallellWorkCapacity());
		}
	}
}