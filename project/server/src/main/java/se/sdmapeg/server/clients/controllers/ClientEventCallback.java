package se.sdmapeg.server.clients.controllers;

import se.sdmapeg.server.clients.callbacks.ClientCallback;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.server.clients.models.Client;
import se.sdmapeg.server.clients.models.ClientManagerModel;
import se.sdmapeg.serverworker.TaskId;

/**
 * A callback to handle client events.
 */
final class ClientEventCallback implements ClientCallback {
	private final ClientManagerModel state;
	private final Client client;

	/**
	 * Creates a new ClientEventCallback to update the specified model when a
	 * client event occurs.
	 *
	 * @param state the model to update
	 * @param client the client to be used as the source for all events
	 */
	public ClientEventCallback(ClientManagerModel state, Client client) {
		this.state = state;
		this.client = client;
	}

	@Override
	public void taskReceived(TaskId taskId, Task<?> task) {
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
