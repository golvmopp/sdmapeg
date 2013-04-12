package se.sdmapeg.server.clients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sdmapeg.common.communication.CommunicationException;
import se.sdmapeg.common.communication.ConnectionClosedException;
import se.sdmapeg.serverclient.communication.ClientToServerMessage;
import se.sdmapeg.serverclient.communication.ClientToServerMessage.Handler;

final class ClientMessageListener implements Runnable {
	private static final Logger LOG =
		LoggerFactory.getLogger(ClientMessageListener.class);
	private final Client client;
	private final ClientToServerMessage.Handler<?> messageHandler;
	private final DisconnectionCallback callback;

	public ClientMessageListener(Client client,
								 Handler<?> messageHandler,
								 DisconnectionCallback callback) {
		this.client = client;
		this.messageHandler = messageHandler;
		this.callback = callback;
	}

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
				try {
					client.receive().accept(messageHandler);
				} catch (ConnectionClosedException e) {
					/*
					 * This means that the connection was closed, so we just
					 * notify the callback and stop listening.
					 */
					callback.clientDisconnected();
					break;
				} catch (CommunicationException e) {
					LOG.error("An error occurred while listening for messages", e);
					client.disconnect();
					callback.clientDisconnected();
					break;
				}
			}
	}

	public interface DisconnectionCallback {
		void clientDisconnected();
	}
}
