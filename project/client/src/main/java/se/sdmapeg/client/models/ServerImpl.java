package se.sdmapeg.client.models;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sdmapeg.common.communication.CommunicationException;
import se.sdmapeg.common.communication.Connection;
import se.sdmapeg.common.communication.ConnectionClosedException;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.serverclient.ClientTaskId;
import se.sdmapeg.serverclient.communication.ClientToServerMessage;
import se.sdmapeg.serverclient.communication.ClientToServerMessageFactory;
import se.sdmapeg.serverclient.communication.ResultMessage;
import se.sdmapeg.serverclient.communication.ServerToClientMessage;

public final class ServerImpl implements Server {
	private static final Logger LOG = LoggerFactory.getLogger(ServerImpl.class);
	private final Connection<ClientToServerMessage, ServerToClientMessage> connection;


	private ServerImpl(
			Connection<ClientToServerMessage, ServerToClientMessage> connection) {
		this.connection = connection;
	}

	@Override
	public InetSocketAddress getAddress() {
		return connection.getAddress();
	}

	@Override
	public void disconnect() {
		try {
			connection.close();
		} catch (IOException ex) {
			LOG.warn("An error occurred while closing the connection", ex);
		}
	}

	@Override
	public void performTask(ClientTaskId taskId, Task<?> task) {
		LOG.info("Sending request to perform task {} to {}", taskId, this);
		send(ClientToServerMessageFactory.newTaskMessage(task, taskId));
	}

	@Override
	public void cancelTask(ClientTaskId taskId) {
		LOG.info("Sending request to cancel task {} to {}", taskId, this);
		send(ClientToServerMessageFactory.newTaskCancellationMessage(taskId));
	}

	@Override
	public void listen(ServerCallback callback) {
		LOG.info("Listening to {}", this);
		ServerToClientMessage.Handler<Void> messageHandler = new MessageHandler(
				callback);
		try {
			while (true) {
				ServerToClientMessage message = connection.receive();
				handleMessage(message, messageHandler);
			}
		} catch (ConnectionClosedException ex) {
			LOG.info("The connection to {} was closed", this);
		} catch (Exception ex) {
			LOG.error("An error occurred while listening for messages", ex);
		} finally {
			disconnect();
			callback.connectionClosed();
		}
	}

	private static void handleMessage(ServerToClientMessage message,
			ServerToClientMessage.Handler<Void> messageHandler) {
		message.accept(messageHandler);
	}

	private void send(ClientToServerMessage message) {
		try {
			connection.send(message);
		} catch (ConnectionClosedException ex) {
			disconnect();
			LOG.warn("Connection to {} was closed while sending a message",
					 this);
		} catch (CommunicationException ex) {
			LOG.error("Failed to send message to " + this, ex);
			disconnect();
		}
	}
	
	@Override
	public String toString() {
		return "Server{" + connection.getAddress() + '}';
	}

	/**
	 * Creates a new Server with the specified connection.
	 *
	 * @param connection A connection to a Server.
	 * @return the new Server.
	 */
	public static Server newServer(Connection<ClientToServerMessage,
			ServerToClientMessage> connection) {
		return new ServerImpl(connection);

	}

	private static final class MessageHandler
			implements ServerToClientMessage.Handler<Void> {
		private final ServerCallback callback; 

		public MessageHandler(ServerCallback callback) {
			this.callback = callback;
		}

		@Override
		public Void handle(ResultMessage message) {
			callback.resultReceived(message.getId(), message.getResult());
			return null;
		}
	}
}
