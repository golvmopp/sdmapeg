package se.sdmapeg.serverclient.communication;

import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.serverclient.ClientTaskId;

public final class ClientToServerMessageFactory {

	/**
	 * Returns an instance of ClientIdentification.
	 * @return an instance of ClientIdentification
	 */
	public static ClientToServerMessage newClientIdentification() {
		return new ClientIdentificationImpl();
	}

	/**
	 * Returns a TaskCancellationMessage.
	 * @return a TaskCancellationMessage
	 */
	public static ClientToServerMessage newTaskCancellationMessagImpl(ClientTaskId clientTaskId) {
		return new TaskCancellationMessageImpl(clientTaskId);
	}

	/**
	 * Returns a TaskMessage.
	 * @return a TaskMessage
	 */
	public static ClientToServerMessage newTaskMessageImpl(Task<?> task, ClientTaskId clientTaskId) {
		return new TaskMessageImpl(task, clientTaskId);
	}

	private static final class ClientIdentificationImpl implements ClientIdentification {
		private static final long serialVersionUID = 0;

		private ClientIdentificationImpl() {
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
		public Task<?> getTask(){
			return task;
		}

		/**
		 * Returns the TaskId of this TaskMessage.
		 * @return the TaskId of this TaskMessage
		 */
		public ClientTaskId getTaskId(){
			return taskID;
		}
	}
}
