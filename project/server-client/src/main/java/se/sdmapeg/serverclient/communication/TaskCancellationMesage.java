package se.sdmapeg.serverclient.communication;

import se.sdmapeg.serverclient.ClientTaskId;

/**
 *
 * @author niclas
 */
public class TaskCancellationMesage implements ClientToServerMessage {
	private final ClientTaskId taskId;

	public TaskCancellationMesage(ClientTaskId taskId) {
		this.taskId = taskId;
	}

	public ClientTaskId getTaskId() {
		return taskId;
	}

	@Override
	public <T> T accept(Handler<T> handler) {
		return handler.handle(this);
	}
}
