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
import se.sdmapeg.serverworker.communication.ResultMessage;
import se.sdmapeg.serverworker.communication.ServerToWorkerMessage;
import se.sdmapeg.serverworker.communication.TaskMessage;
import se.sdmapeg.serverworker.communication.WorkerIdentification;
import se.sdmapeg.serverworker.communication.WorkerToServerMessage;

public final class WorkerImpl implements Worker {
	private static final Logger LOG = LoggerFactory.getLogger(WorkerImpl.class);
	private final Connection<ServerToWorkerMessage, WorkerToServerMessage> connection;
	private final Worker.Callback callback;
	private final WorkerToServerMessage.Handler<Void> messageHandler =
		new MessageHander();
	private final Set<TaskId> activeTasks = Collections.newSetFromMap(
			new ConcurrentHashMap<TaskId, Boolean>());
	private final Object taskAssignmentLock = new Object();
	private volatile boolean acceptingWork = true;
	private volatile WorkerData workerData = WorkerData.getDefaultWorkerData();

	public WorkerImpl(Connection<ServerToWorkerMessage,
			WorkerToServerMessage> connection, Callback callback) {
		this.connection = connection;
		this.callback = callback;
	}

	@Override
	public InetAddress getAddress() {
		return connection.getAddress();
	}

	@Override
	public boolean assignTask(TaskId taskId, Task<?> task) {
		synchronized (taskAssignmentLock) {
			if (!isAcceptingWork()) {
				return false;
			}
			activeTasks.add(taskId);
		}
		send(TaskMessage.newTaskMessage(task, taskId));
		return true;
	}

	@Override
	public void cancelTask(TaskId taskId) {
		if (activeTasks.remove(taskId)) {
			// TODO: Send message to cancel task here
		}
	}

	@Override
	public void stealTasks(int max) {
		/*
		 * Implementing this as a no-op is completely valid, as per the
		 * contract.
		 */
		// TODO: Add code to actually do something here
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
	public void listen() {
		try {
			while (true) {
				WorkerToServerMessage message = connection.receive();
				handleMessage(message);
			}
		} catch (ConnectionClosedException ex) {
			LOG.info("{} disconnected", this);
		} catch (CommunicationException ex) {
			LOG.error("An error occurred while listening for messages", ex);
		} finally {
			synchronized (taskAssignmentLock) {
				acceptingWork = false;
			}
			disconnect();
			callback.workerDisconnected(this);
		}
	}

	private void handleMessage(WorkerToServerMessage message) {
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

	private void completeTask(TaskId taskId, Result<?> result) {
		activeTasks.remove(taskId);
		callback.taskCompleted(this, taskId, result);
		if (!isWorkingAtFullCapacity()) {
			requestWork();
		}
	}

	private boolean isWorkingAtFullCapacity() {
		return getLoad() >= 0 || !isAcceptingWork();
	}

	private void requestWork() {
		callback.workRequested(this);
	}

	private static WorkerData extractWorkerData(WorkerIdentification message) {
		return new WorkerData(message.getParallelWorkCapacity());
	}

	private final class MessageHander implements WorkerToServerMessage.Handler<Void> {

		@Override
		public Void handle(ResultMessage message) {
			TaskId taskId = message.getId();
			Result<?> result = message.getResult();
			completeTask(taskId, result);
			return null;
		}

		@Override
		public Void handle(WorkerIdentification message) {
			workerData = extractWorkerData(message);
			if (!isWorkingAtFullCapacity()) {
				requestWork();
			}
			return null;
		}
	}

	private enum WorkerIdentificationExtractor
			implements WorkerToServerMessage.Handler<WorkerIdentification> {
		INSTANCE;

		@Override
		public WorkerIdentification handle(ResultMessage message) {
			throw new IllegalArgumentException();
		}

		@Override
		public WorkerIdentification handle(WorkerIdentification message) {
			return message;
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
