package se.sdmapeg.project.testapp;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author niclas
 */
public final class ThreadedConnectionHandler implements ConnectionHandler {
	private final ExecutorService connectionThreadPool =
		Executors.newCachedThreadPool();
	private final Set<Connection<ServerMessage, ClientMessage>>
		connections = Collections.newSetFromMap(
			new ConcurrentHashMap<Connection<ServerMessage, ClientMessage>,
				Boolean>());
	private final ConnectionCallback connectionCallback;

	public ThreadedConnectionHandler(ConnectionCallback connectionCallback) {
		this.connectionCallback = connectionCallback;
	}

	@Override
	public void handle(Connection<ServerMessage, ClientMessage> connection) {
		connectionThreadPool.submit(new ConnectionMessageListener(connection));
	}

	@Override
	public void serverShutdown() {
		for (Connection<ServerMessage, ClientMessage> connection : connections) {
			try {
				connection.close();
			}
			catch (IOException ex) {
				//TODO: Log this
			}
		}
		connectionThreadPool.shutdown();
	}

	public interface MessageHandler {
		void handle(ClientMessage message);
	}

	public interface ConnectionCallback {
		MessageHandler connectionOpened(
				Connection<ServerMessage, ClientMessage> connection);
		void connectionClosed(
				Connection<ServerMessage, ClientMessage> connection);
	}

	private class ConnectionMessageListener implements Runnable {
		private final Connection<ServerMessage, ClientMessage> connection;
		private final MessageHandler messageHandler;

		public ConnectionMessageListener(
				Connection<ServerMessage, ClientMessage> connection) {
			this.connection = connection;
			this.messageHandler = connectionCallback.connectionOpened(connection);
			connections.add(connection);
		}

		@Override
		public void run() {
			try {
				while (!Thread.currentThread().isInterrupted()) {
					messageHandler.handle(connection.receiveMessage());
				}
			}
			catch (IOException ex) {
				//TODO: Log this
			}
			finally {
				close();
			}
		}

		private void close() {
			try {
				connection.close();
			}
			catch (IOException ex) {
				//TODO: Log this
			}
			connections.remove(connection);
			connectionCallback.connectionClosed(connection);
		}
	}
}
