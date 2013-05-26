package se.sdmapeg.server.clients.controllers;

import se.sdmapeg.server.clients.exceptions.ClientRejectedException;
import java.util.concurrent.ExecutorService;
import se.sdmapeg.common.IdGenerator;
import se.sdmapeg.common.communication.Connection;
import se.sdmapeg.server.clients.models.Client;
import se.sdmapeg.server.clients.models.ClientImpl;
import se.sdmapeg.server.clients.models.ClientManagerModel;
import se.sdmapeg.server.communication.ConnectionAcceptorCallback;
import se.sdmapeg.serverclient.communication.ClientToServerMessage;
import se.sdmapeg.serverclient.communication.ServerToClientMessage;
import se.sdmapeg.serverworker.TaskId;

/**
 * A callback to handle new client connections.
 */
public final class ClientConnectionCallback
		implements ConnectionAcceptorCallback<ServerToClientMessage,
			ClientToServerMessage> {
	private final ClientManagerModel state;
	private final ExecutorService connectionThreadPool;
	private final IdGenerator<TaskId> taskIdGenerator;

	/**
	 * Creates a new ClientConnectionCallback, with the specified
	 * ClientManagerModel to update, ExecutorService to execute new listener
	 * tasks, and task ID generator to be used by the new clients to generate
	 * task IDs.
	 *
	 * @param state the model to update with new clients
	 * @param connectionThreadPool the thread pool to be used for listening to
	 *                             new clients
	 * @param taskIdGenerator the ID generator to be used by new clients
	 */
	public ClientConnectionCallback(ClientManagerModel state,
			ExecutorService connectionThreadPool,
			IdGenerator<TaskId> taskIdGenerator) {
		this.state = state;
		this.connectionThreadPool = connectionThreadPool;
		this.taskIdGenerator = taskIdGenerator;
	}

	@Override
	public void connectionReceived(Connection<ServerToClientMessage,
			ClientToServerMessage> connection) {
		final Client client = ClientImpl.newClient(connection, taskIdGenerator);
		try {
			state.addClient(client);
			connectionThreadPool.submit(new Runnable() {
				@Override
				public void run() {
					client.listen(new ClientEventCallback(state, client));
				}
			});
		}
		catch (ClientRejectedException ex) {
			client.disconnect();
		}
	}

	@Override
	public void connectionHandlerClosed() {
		/*
		 * Disconnect all currently connected clients. Since this method is
		 * called by the only thread responsible for accepting new
		 * connections, we can safely assume that the collection will
		 * remain up to date without having to worry about new clients being
		 * added concurrently.
		 */
		for (Client client : state.getClients()) {
			client.disconnect();
		}
	}
	
}
