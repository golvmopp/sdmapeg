package se.sdmapeg.serverclient.communication;

import se.sdmapeg.serverclient.ClientTaskId;

/**
 *
 * @author niclas
 */
public class TaskCancellationMessage implements ClientToServerMessage {
	private final ClientTaskId clientTaskId;

	private TaskCancellationMessage(ClientTaskId clientTaskId) {
		this.clientTaskId = clientTaskId;
	}

	public ClientTaskId getTaskId() {
		return clientTaskId;
	}

	@Override
	public <T> T accept(Handler<T> handler) {
		return handler.handle(this);
	}

	public static ClientToServerMessage newTaskCancellationMessage(ClientTaskId taskId) {
		return new TaskCancellationMessage(taskId);
	}
}
