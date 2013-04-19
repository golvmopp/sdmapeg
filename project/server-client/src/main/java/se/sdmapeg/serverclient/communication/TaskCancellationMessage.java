package se.sdmapeg.serverclient.communication;

import se.sdmapeg.serverclient.ClientTaskId;

/**
 *
 * @author niclas
 */
public class TaskCancellationMessage implements ClientToServerMessage {
	private final ClientTaskId taskId;

	private TaskCancellationMessage(ClientTaskId taskId) {
		this.taskId = taskId;
	}

	public ClientTaskId getTaskId() {
		return taskId;
	}

	@Override
	public <T> T accept(Handler<T> handler) {
		return handler.handle(this);
	}

	public static TaskCancellationMessage newTaskCancellationMessage(ClientTaskId taskId) {
		return new TaskCancellationMessage(taskId);
	}
}
