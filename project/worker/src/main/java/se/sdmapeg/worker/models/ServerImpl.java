package se.sdmapeg.worker.models;

import se.sdmapeg.common.communication.Connection;
import se.sdmapeg.serverworker.communication.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sdmapeg.common.communication.CommunicationException;
import se.sdmapeg.common.communication.ConnectionClosedException;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.serverworker.TaskId;
import se.sdmapeg.serverworker.communication.ServerToWorkerMessage.Handler;

/**
 * Implementation of Server interface.
 */
public final class ServerImpl implements Server {
	private static final Logger LOG = LoggerFactory.getLogger(ServerImpl.class);
	private final Connection<WorkerToServerMessage, ServerToWorkerMessage> connection;

	private ServerImpl(Connection<WorkerToServerMessage,
			ServerToWorkerMessage> connection) {
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
		} catch (IOException e) {
			LOG.warn("An error occurred while closing the connection", e);
		}
	}

	@Override
	public void listen(ServerCallback callback) {
		LOG.info("Listening to {}", this);
		ServerToWorkerMessage.Handler<Void> messageHandler = new MessageHandler(
				callback);
		try {
			while (true) {
				ServerToWorkerMessage message = connection.receive();
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

	private void send(WorkerToServerMessage message) {
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

	@Override
	public void taskCompleted(TaskId taskId, Result<?> result) {
		LOG.info("Sending result for task {} to {}", taskId, this);
		send(WorkerToServerMessageFactory.newResultMessage(taskId, result));
	}

	@Override
	public void tasksStolen(Set<TaskId> tasks) {
		LOG.info("Sending stolen tasks {} to {}", tasks, this);
		send(WorkerToServerMessageFactory.newWorkStealingResponseMessage(tasks));
	}

	@Override
	public void identify(int parallelWorkCapacity) {
		LOG.info("Sending identification message to {}");
		send(WorkerToServerMessageFactory.newWorkerIdentificationMessage(parallelWorkCapacity));
	}

	/**
	 * Creates a new Server with the specified connection.
	 *
	 * @param connection A connection to a Server.
	 * @return the Server.
	 */
	public static Server newServer(Connection<WorkerToServerMessage,
			ServerToWorkerMessage> connection) {
		return new ServerImpl(connection);
	}

	private static void handleMessage(ServerToWorkerMessage message,
							   Handler<Void> messageHandler) {
		message.accept(messageHandler);
	}

	private static final class MessageHandler
			implements ServerToWorkerMessage.Handler<Void> {
		private final ServerCallback callback;

		public MessageHandler(ServerCallback callback) {
			this.callback = callback;
		}

		@Override
		public Void handle(TaskMessage message) {
			callback.taskReceived(message.getTaskId(), message.getTask());
			return null;
		}

		@Override
		public Void handle(TaskCancellationMessage message) {
			callback.taskCancelled(message.getTaskId());
			return null;
		}

		@Override
		public Void handle(WorkStealingRequestMessage message) {
			callback.workStealingRequested(message.getDesired());
			return null;
		}
	}
}
