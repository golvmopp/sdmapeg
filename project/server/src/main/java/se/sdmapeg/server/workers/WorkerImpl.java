package se.sdmapeg.server.workers;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sdmapeg.common.communication.CommunicationException;
import se.sdmapeg.common.communication.Connection;
import se.sdmapeg.common.communication.ConnectionClosedException;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.serverworker.TaskId;
import se.sdmapeg.serverworker.communication.*;


/**
 * A Worker representation using an underlying Connection to communicate with
 * the actual worker.
 */
final class WorkerImpl implements Worker {
	private static final Logger LOG = LoggerFactory.getLogger(WorkerImpl.class);
	private final Connection<ServerToWorkerMessage, WorkerToServerMessage> connection;
	private final Set<TaskId> activeTasks = Collections.newSetFromMap(
			new ConcurrentHashMap<TaskId, Boolean>());
	private final Object taskAssignmentLock = new Object();
	private volatile boolean acceptingWork = true;
	private volatile WorkerData workerData = WorkerData.getDefaultWorkerData();

	private WorkerImpl(Connection<ServerToWorkerMessage,
								 WorkerToServerMessage> connection) {
		this.connection = connection;
	}

	@Override
	public InetAddress getAddress() {
		return connection.getAddress();
	}

	@Override
	public void assignTask(TaskId taskId, Task<?> task)
			throws TaskRejectedException {
		synchronized (taskAssignmentLock) {
			if (!isAcceptingWork()) {
				throw new TaskRejectedException();
			}
			activeTasks.add(taskId);
		}
		send(ServerToWorkerMessageFactory.newTaskMessage(task, taskId));
	}

	@Override
	public void cancelTask(TaskId taskId) {
		if (activeTasks.remove(taskId)) {
			LOG.info("Sending task cancellation message to {}", this);
			send(ServerToWorkerMessageFactory.newTaskCancellationMessage(taskId));
		}
	}

	@Override
	public void stealTasks(int max) {
		LOG.info("Sending task stealing request to {}", this);
		send(ServerToWorkerMessageFactory.newWorkStealingRequestMessage(max));
	}

	@Override
	public Set<TaskId> getActiveTasks() {
		return Collections.unmodifiableSet(new HashSet<>(activeTasks));
	}

	@Override
	public int getLoad() {
		return activeTasks.size() - getParallellWorkCapacity();
	}

	@Override
	public boolean isAcceptingWork() {
		return acceptingWork;
	}

	@Override
	public void disconnect() {
		try {
			connection.close();
		} catch (IOException e) {
			LOG.warn("An error occurred while closing the connection", e);
		}
	}

	@Override
	public int getParallellWorkCapacity() {
		return workerData.getParallelWorkCapacity();
	}

	@Override
	public void listen(WorkerCallback callback) {
		LOG.info("Listening to {}", this);
		WorkerToServerMessage.Handler<Void> messageHandler = new MessageHander(
				callback);
		try {
			while (true) {
				WorkerToServerMessage message = connection.receive();
				handleMessage(message, messageHandler);
			}
		} catch (ConnectionClosedException ex) {
			LOG.info("{} disconnected", this);
		} catch (Exception ex) {
			LOG.error("An error occurred while listening for messages", ex);
		} finally {
			synchronized (taskAssignmentLock) {
				acceptingWork = false;
			}
			disconnect();
			callback.workerDisconnected();
		}
	}

	private void handleMessage(WorkerToServerMessage message,
			WorkerToServerMessage.Handler<Void> messageHandler) {
		message.accept(messageHandler);
	}

	@Override
	public String toString() {
		return "Worker{" + connection.getAddress() + '}';
	}

	private void send(ServerToWorkerMessage message) {
		try {
			connection.send(message);
		} catch (ConnectionClosedException ex) {
			disconnect();
			LOG.warn("{} was disconnected", this);
		} catch (CommunicationException ex) {
			LOG.error("Failed to send message to " + this, ex);
			disconnect();
		}
	}

	private void completeTask(TaskId taskId, Result<?> result,
			WorkerCallback callback) {
		activeTasks.remove(taskId);
		callback.taskCompleted(taskId, result);
		if (!isWorkingAtFullCapacity()) {
			requestWork(callback);
		}
	}

	private boolean isWorkingAtFullCapacity() {
		return getLoad() >= 0 || !isAcceptingWork();
	}

	private void requestWork(WorkerCallback callback) {
		callback.workRequested();
	}

	private static WorkerData extractWorkerData(WorkerIdentificationMessage message) {
		return new WorkerData(message.getParallelWorkCapacity());
	}

	/**
	 * Creates a new worker representation communicating over the specified
	 * connection.
	 *
	 * @param connection		the Connection to be used for communicating with
	 *							this worker
	 * @return the created Worker
	 */
	public static Worker newWorker(Connection<ServerToWorkerMessage,
								 WorkerToServerMessage> connection) {
		return new WorkerImpl(connection);
	}

	private final class MessageHander
			implements WorkerToServerMessage.Handler<Void> {
		private final WorkerCallback callback;

		public MessageHander(WorkerCallback callback) {
			this.callback = callback;
		}

		@Override
		public Void handle(ResultMessage message) {
			TaskId taskId = message.getId();
			Result<?> result = message.getResult();
			completeTask(taskId, result, callback);
			return null;
		}

		@Override
		public Void handle(WorkerIdentificationMessage message) {
			workerData = extractWorkerData(message);
			if (!isWorkingAtFullCapacity()) {
				requestWork(callback);
			}
			return null;
		}

		@Override
		public Void handle(WorkStealingResponseMessage message) {
			for (TaskId taskId : message.getStolenTasks()) {
				activeTasks.remove(taskId);
				callback.taskStolen(taskId);
			}
			return null;
		}
	}

	private static final class WorkerData {
		private static final WorkerData DEFAULT = new WorkerData(1); 
		private final int parallelWorkCapacity;

		public WorkerData(int parallelWorkCapacity) {
			this.parallelWorkCapacity = parallelWorkCapacity;
		}

		public int getParallelWorkCapacity() {
			return parallelWorkCapacity;
		}

		public static WorkerData getDefaultWorkerData() {
			return DEFAULT;
		}
	}
}
