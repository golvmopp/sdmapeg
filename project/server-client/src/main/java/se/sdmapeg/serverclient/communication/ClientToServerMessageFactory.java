package se.sdmapeg.serverclient.communication;

import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.serverclient.ClientTaskId;

public final class ClientToServerMessageFactory {

	/**
	 * Returns an instance of ClientIdentificationMessage.
	 * @return an instance of ClientIdentificationMessage
	 */
	public static ClientToServerMessage newClientIdentificationMessage() {
		return new ClientIdentificationMessageImpl();
	}

	/**
	 * Returns a TaskCancellationMessage.
	 * @return a TaskCancellationMessage
	 */
	public static ClientToServerMessage newTaskCancellationMessage(ClientTaskId clientTaskId) {
		return new TaskCancellationMessageImpl(clientTaskId);
	}

	/**
	 * Returns a TaskMessage.
	 * @return a TaskMessage
	 */
	public static ClientToServerMessage newTaskMessage(Task<?> task, ClientTaskId clientTaskId) {
		return new TaskMessageImpl(task, clientTaskId);
	}

	private static final class ClientIdentificationMessageImpl implements ClientIdentificationMessage {
		private static final long serialVersionUID = 0;

		private ClientIdentificationMessageImpl() {
			super();
		}

		@Override
		public <T> T accept(Handler<T> handler) {
			return handler.handle(this);
		}
	}

	private static final class TaskCancellationMessageImpl implements TaskCancellationMessage {
		private final ClientTaskId clientTaskId;

		private TaskCancellationMessageImpl(ClientTaskId clientTaskId) {
			this.clientTaskId = clientTaskId;
		}

		@Override
		public ClientTaskId getTaskId() {
			return clientTaskId;
		}

		@Override
		public <T> T accept(Handler<T> handler) {
			return handler.handle(this);
		}
	}

	private static final class TaskMessageImpl implements TaskMessage {
		private static final long serialVersionUID = -5680777161207194946L;
		private final Task<?> task;
		private final ClientTaskId taskID;

		private TaskMessageImpl(Task<?> task, ClientTaskId taskID){
			this.task = task;
			this.taskID = taskID;
		}

		@Override
		public <T> T accept(Handler<T> handler) {
			return handler.handle(this);
		}

		/**
		 * Returns the Task of this TaskMessage.
		 * @return the Task of this TaskMessage
		 */
		@Override
		public Task<?> getTask(){
			return task;
		}

		/**
		 * Returns the TaskId of this TaskMessage.
		 * @return the TaskId of this TaskMessage
		 */
		@Override
		public ClientTaskId getTaskId(){
			return taskID;
		}
	}
}
