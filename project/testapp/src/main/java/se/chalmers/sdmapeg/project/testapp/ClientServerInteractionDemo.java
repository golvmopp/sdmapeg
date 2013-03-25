package se.chalmers.sdmapeg.project.testapp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author niclas
 */
public class ClientServerInteractionDemo {
	private static final int PORT = 6666;

	public static void interact() {
		try {
			ServerDemo server = new ServerDemo(PORT, new ThreadedConnectionHandler(
					new ConnectionCallback()));
			ClientDemo client = new ClientDemo();
			try (Connection<ClientMessage, ServerMessage> connection =
									  client.connectTo(new InetSocketAddress(
						"localhost", PORT))) {
				ExecutorService clientListener =
								Executors.newSingleThreadExecutor();
				clientListener.submit(new ClientListener(connection));
				connection.sendMessage(new Ping("Hello Server!"));
				connection.sendMessage(new Ping("I'm a client!"));
				connection.sendMessage(new Ping("Is this working?"));
				try {
					Thread.sleep(3000);
				}
				catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
				clientListener.shutdownNow();
			}
			finally {
				server.shutdown();
			}
		}
		catch (IOException ex) {
			throw new AssertionError(ex);
		}
	}

	private static class ClientListener implements Runnable {
		private final Connection<ClientMessage, ServerMessage> connection;

		public ClientListener(
				Connection<ClientMessage, ServerMessage> connection) {
			this.connection = connection;
		}

		@Override
		public void run() {
			try {
				while (!Thread.currentThread().isInterrupted()) {
					ServerMessage message = connection.receiveMessage();
					System.out.println("Client: The server said: " + message);
				}
			}
			catch (IOException ex) {
			}
		}
	}

	private static class SimpleConnectionHandler implements ConnectionHandler {
		private final ExecutorService executors =
									  Executors.newCachedThreadPool();
		private final Set<Connection<ServerMessage, ClientMessage>> connections =
																	new CopyOnWriteArraySet<>();

		@Override
		public void handle(Connection<ServerMessage, ClientMessage> connection) {
			connections.add(connection);
			executors.submit(new Handler(connection));
		}

		@Override
		public void serverShutdown() {
			executors.shutdownNow();
			for (Connection<ServerMessage, ClientMessage> connection :
				 connections) {
				try {
					connection.close();
				}
				catch (IOException ex) {
					throw new AssertionError(ex);
				}
			}
		}

		private static class Handler implements Runnable {
			private final Connection<ServerMessage, ClientMessage> connection;

			public Handler(Connection<ServerMessage, ClientMessage> connection) {
				this.connection = connection;
			}

			@Override
			public void run() {
				try {
					while (!Thread.currentThread().isInterrupted()) {
						ClientMessage message = connection.receiveMessage();
						if (message instanceof Ping) {
							Ping ping = (Ping) message;
							System.out.println("Server: The client said: "
											   + ping);
							connection.sendMessage(new Pong(ping.toString()));
						}
					}
				}
				catch (IOException ex) {
				}
			}
		}
	}

	private static class ConnectionCallback implements ThreadedConnectionHandler.ConnectionCallback {
		@Override
		public MessageHandler connectionOpened(Connection<ServerMessage, ClientMessage> connection) {
			return new MessageHandler(connection);
		}

		@Override
		public void connectionClosed(Connection<ServerMessage, ClientMessage> connection) {
			//Nothing to do here
		}

		private static class MessageHandler implements ThreadedConnectionHandler.MessageHandler {
			private final Connection<ServerMessage, ClientMessage> connection;

			public MessageHandler(Connection<ServerMessage, ClientMessage> connection) {
				this.connection = connection;
			}

			@Override
			public void handle(ClientMessage message) {
				try {
					if (message instanceof Ping) {
						Ping ping = (Ping) message;
						System.out.println("Server: The client said: "
										   + ping);
						connection.sendMessage(new Pong(ping.toString()));
					}
				}
				catch (IOException ex) {
					throw new AssertionError(ex);
				}
			}

		}
	}

	private static class Ping implements ClientMessage {
		private final String ping;

		public Ping(String ping) {
			this.ping = ping;
		}

		public String getPing() {
			return ping;
		}

		@Override
		public String toString() {
			return "Ping! " + ping;
		}
	}

	private static class Pong implements ServerMessage {
		private final String pong;

		public Pong(String pong) {
			this.pong = pong;
		}

		public String getPong() {
			return pong;
		}

		@Override
		public String toString() {
			return "Pong! " + pong;
		}
	}
}
