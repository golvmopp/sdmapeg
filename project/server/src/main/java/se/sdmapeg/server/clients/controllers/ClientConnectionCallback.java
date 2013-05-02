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
 *
 * @author niclas
 */
public final class ClientConnectionCallback implements ConnectionAcceptorCallback<ServerToClientMessage, ClientToServerMessage> {
	private final ClientManagerModel state;
	private final ExecutorService connectionThreadPool;
	private final IdGenerator<TaskId> taskIdGenerator;

	public ClientConnectionCallback(ClientManagerModel state,
									ExecutorService connectionThreadPool,
									IdGenerator<TaskId> taskIdGenerator) {
		this.state = state;
		this.connectionThreadPool = connectionThreadPool;
		this.taskIdGenerator = taskIdGenerator;
	}

	@Override
	public void connectionReceived(Connection<ServerToClientMessage, ClientToServerMessage> connection) {
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
