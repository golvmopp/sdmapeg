package se.sdmapeg.server.clients;

import java.net.SocketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sdmapeg.common.communication.CommunicationException;
import se.sdmapeg.server.communication.ConnectionHandler;
import se.sdmapeg.serverclient.communication.ClientToServerMessage;
import se.sdmapeg.serverclient.communication.ServerToClientMessage;

/**
 *
 * @author niclas
 */
final class ClientConnectionAcceptor implements Runnable {
	private static final Logger LOG = LoggerFactory
			.getLogger(ClientConnectionAcceptor.class);
	private final ConnectionHandler<ServerToClientMessage, ClientToServerMessage> connectionHandler;
	private final Callback callback;

	public ClientConnectionAcceptor(
			ConnectionHandler<ServerToClientMessage, ClientToServerMessage> connectionHandler,
									Callback callback) {
		this.connectionHandler = connectionHandler;
		this.callback = callback;
	}

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				callback.clientConnected(ClientImpl.newClient(
						connectionHandler.accept()));
			}
			catch (CommunicationException e) {
				LOG.error("An error occured while waiting for connections", e);
			}
			catch (SocketException e) {
				if (!connectionHandler.isOpen()) {
					callback.shutdown();
					break;
				}
				else {
					LOG.error("An error occured while waiting for connections",
							  e);
				}
			}
		}
	}

	public interface Callback {
		void clientConnected(Client client);

		void shutdown();
	}
}
