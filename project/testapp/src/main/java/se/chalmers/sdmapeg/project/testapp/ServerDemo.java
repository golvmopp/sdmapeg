package se.chalmers.sdmapeg.project.testapp;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class representing a simple server.
 *
 * @author niclas
 */
public class ServerDemo {
	private final ExecutorService connectionListenerExecutor =
		Executors.newSingleThreadExecutor();
	private final ServerSocket serverSocket;
	private boolean running;

	public ServerDemo(int port, ConnectionHandler connectionHandler)
			throws IOException {
		this(new ServerSocket(port), connectionHandler);
	}

	private ServerDemo(ServerSocket serverSocket,
					   ConnectionHandler connectionHandler) {
		this.serverSocket = serverSocket;
		this.running = true;
		connectionListenerExecutor.submit(
				new ConnectionAcceptor(serverSocket, connectionHandler));
	}

	public synchronized boolean isRunning() {
		return running;
	}

	/**
	 * Shuts down this server if it has not already been shut down. Calling this
	 * method on a server that is no longer running has no effect.
	 */
	public synchronized void shutdown() {
		if (!isRunning()) {
			return;
		}
		running = false;
		try {
			serverSocket.close();
		}
		catch (IOException ex) {
			throw new AssertionError(ex);
		}
		connectionListenerExecutor.shutdown();
	}

	public interface ConnectionHandler {
		void handle(Connection<ServerMessage, ClientMessage> connection);
		void serverShutdown();
	}

	private static class ConnectionAcceptor implements Runnable {
		private final ServerSocket serverSocket;
		private final ConnectionHandler connectionHandler;

		public ConnectionAcceptor(ServerSocket serverSocket,
								  ConnectionHandler connectionHandler) {
			this.serverSocket = serverSocket;
			this.connectionHandler = connectionHandler;
		}

		@Override
		public void run() {
			try {
				while (true) {
					connectionHandler.handle(
							new Connection<ServerMessage, ClientMessage>(
								serverSocket.accept()));
				}
			}
			catch (IOException ex) {
				connectionHandler.serverShutdown();
			}
		}
	}
}
