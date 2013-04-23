package se.sdmapeg.serverworker.communication;

import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.serverworker.TaskId;

public final class ServerToWorkerMessageFactory {

	/**
	 * Returns a TaskCancellationMessage.
	 * @return a TaskCancellationMessage
	 */
	public static ServerToWorkerMessage newTaskCancellationMessage(TaskId taskId) {
		return new TaskCancellationMessageImpl(taskId);
	}

	/**
	 * Returns a new TaskMessage.
	 *
	 * @param task The task to be sent to the worker
	 * @param taskID The TaskId linked with the Task
	 * @return the new ResultMessage
	 */
	public static ServerToWorkerMessage newTaskMessage(Task<?> task,
	                                                   TaskId taskID){
		return new TaskMessageImpl(task, taskID);
	}

	/**
	 * Returns a WorkStealingRequestMessage.
	 * @return a WorkStealingRequestMessage
	 */
	public static WorkStealingRequestMessage newWorkStealingRequestMessage(int desired){
		return new WorkStealingRequestMessageImpl(desired);
	}

	private static class TaskCancellationMessageImpl implements TaskCancellationMessage {
		private final TaskId taskId;

		private TaskCancellationMessageImpl(TaskId taskId) {
			this.taskId = taskId;
		}

		public TaskId getTaskId() {
			return taskId;
		}

		@Override
		public <T> T accept(Handler<T> handler) {
			return handler.handle(this);
		}
	}

	private static class TaskMessageImpl implements TaskMessage {
		private static final long serialVersionUID = 368380099586489028L;
		private final Task<?> task;
		private final TaskId taskID;

		private TaskMessageImpl(Task<?> task, TaskId taskID){
			this.task = task;
			this.taskID = taskID;
		}

		@Override
		public <T> T accept(ServerToWorkerMessage.Handler<T> handler) {
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
		public TaskId getTaskId(){
			return taskID;
		}
	}

	private static class WorkStealingRequestMessageImpl implements WorkStealingRequestMessage {
		public int desired;

		private WorkStealingRequestMessageImpl(int desired){
			this.desired = desired;
		}

		@Override
		public <T> T accept(ServerToWorkerMessage.Handler<T> handler) {
			return handler.handle(this);
		}

		public int getDesired() {
			return desired;
		}
	}
}
