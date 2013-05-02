package se.sdmapeg.server.clients.controllers;

import se.sdmapeg.server.clients.callbacks.ClientCallback;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.server.clients.models.Client;
import se.sdmapeg.server.clients.models.ClientManagerModel;
import se.sdmapeg.serverworker.TaskId;

/**
 *
 * @author niclas
 */
final class ClientEventCallback implements ClientCallback {
	private final ClientManagerModel state;
	private final Client client;

	public ClientEventCallback(ClientManagerModel state, Client client) {
		this.state = state;
		this.client = client;
	}

	@Override
	public void taskReceived(TaskId taskId,
							 Task<?> task) {
		state.addTask(client, taskId, task);
	}

	@Override
	public void taskCancelled(TaskId taskId) {
		state.cancelTask(taskId);
	}

	@Override
	public void clientDisconnected() {
		state.removeClient(client);
	}
	
}
