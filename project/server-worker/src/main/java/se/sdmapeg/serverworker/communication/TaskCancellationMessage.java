package se.sdmapeg.serverworker.communication;

import se.sdmapeg.serverworker.TaskId;

/**
 *
 * @author niclas
 */
public class TaskCancellationMessage implements ServerToWorkerMessage {
	private final TaskId taskId;

	private TaskCancellationMessage(TaskId taskId) {
		this.taskId = taskId;
	}

	public TaskId getTaskId() {
		return taskId;
	}

	@Override
	public <T> T accept(Handler<T> handler) {
		return handler.handle(this);
	}

	public static TaskCancellationMessage newTaskCancellationMessage(TaskId taskId) {
		return new TaskCancellationMessage(taskId);
	}
}
