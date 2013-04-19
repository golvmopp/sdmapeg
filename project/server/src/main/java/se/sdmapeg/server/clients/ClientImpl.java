package se.sdmapeg.server.clients;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sdmapeg.common.IdGenerator;
import se.sdmapeg.common.communication.CommunicationException;
import se.sdmapeg.common.communication.Connection;
import se.sdmapeg.common.communication.ConnectionClosedException;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.serverclient.ClientTaskId;
import se.sdmapeg.serverclient.communication.*;
import se.sdmapeg.serverclient.communication.ClientToServerMessage.Handler;
import se.sdmapeg.serverclient.communication.TaskCancellationMessage;
import se.sdmapeg.serverworker.TaskId;

/**
 * A Client representation using an underlying Connection to communicate with
 * the actual client.
 */
final class ClientImpl implements Client {
	private static final Logger LOG = LoggerFactory.getLogger(ClientImpl.class);
	private final Connection<ServerToClientMessage, ClientToServerMessage> connection;
	private final IdGenerator<TaskId> taskIdGenerator;
	private final Map<TaskId, ClientTaskId> taskIdMap =
		new ConcurrentHashMap<>();
	private final Map<ClientTaskId, TaskId> clientTaskIdMap =
		new ConcurrentHashMap<>();
	

	private ClientImpl(Connection<ServerToClientMessage,
								 ClientToServerMessage> connection,
			IdGenerator<TaskId> taskIdGenerator) {
		this.connection = connection;
		this.taskIdGenerator = taskIdGenerator;
	}

	@Override
	public InetAddress getAddress() {
		return connection.getAddress();
	}

	@Override
	public void listen(ClientCallback callback) {
		try {
			LOG.info("Listening to {}", this);
			ClientToServerMessage.Handler<Void> messageHandler =
				new MessageHandler(callback);
			while (true) {
				ClientToServerMessage message = connection.receive();
				handleMessage(message, messageHandler);
			}
		} catch (ConnectionClosedException ex) {
			LOG.info("{} disconnected", this);
		} catch (Exception ex) {
			LOG.error("An error occurred while listening for messages", ex);
		} finally {
			disconnect();
			callback.clientDisconnected();
		}
	}

	@Override
	public void taskCompleted(TaskId taskId, Result<?> result) {
		ClientTaskId clientTaskId = taskIdMap.remove(taskId);
		clientTaskIdMap.remove(clientTaskId);
		if (clientTaskId == null) {
			return;
		}
		send(ResultMessage.newResultMessage(clientTaskId, result));
	}

	@Override
	public Set<TaskId> getActiveTasks() {
		return Collections.unmodifiableSet(new HashSet<>(taskIdMap.keySet()));
	}

	@Override
	public void disconnect() {
		try {
			connection.close();
		} catch (IOException ex) {
			LOG.warn("An error occurred while closing the connection to "
					 + this, ex);
		}
	}

	private void send(ServerToClientMessage message) {
		try {
			connection.send(message);
		} catch (ConnectionClosedException ex) {
			disconnect();
			LOG.warn("{} was disconnected", this);
		} catch (CommunicationException ex) {
			LOG.error("Failed to send message to " + this, ex);
			disconnect();
		}
	}

	@Override
	public String toString() {
		return "Client{" + connection.getAddress() + '}';
	}

	private static void handleMessage(ClientToServerMessage message,
							   Handler<Void> messageHandler) {
		message.accept(messageHandler);
	}

	/**
	 * Creates a new client representation communicating over the specified
	 * connection.
	 *
	 * @param connection		the Connection to be used for communicating with
	 *							this client
	 * @param taskIdGenerator	the IdGenerator to be used by this client for
	 *							generating new TaskIds
	 * @return the created Client
	 */
	public static Client newClient(Connection<ServerToClientMessage,
								 ClientToServerMessage> connection,
			IdGenerator<TaskId> taskIdGenerator) {
		return new ClientImpl(connection, taskIdGenerator);
	}

	private final class MessageHandler
			implements ClientToServerMessage.Handler<Void> {
		private final ClientCallback callback;

		public MessageHandler(ClientCallback callback) {
			this.callback = callback;
		}

		@Override
		public Void handle(ClientIdentification message) {
			LOG.error("Received unexpected message type");
			disconnect();
			callback.clientDisconnected();
			return null;
		}

		@Override
		public Void handle(TaskMessage message) {
			Task<?> task = message.getTask();
			ClientTaskId clientTaskId = message.getTaskId();
			TaskId taskId = taskIdGenerator.newId();
			taskIdMap.put(taskId, clientTaskId);
			clientTaskIdMap.put(clientTaskId, taskId);
			callback.taskReceived(taskId, task);
			return null;
		}

		@Override
		public Void handle(TaskCancellationMessage mesage) {
			ClientTaskId clientTaskId = mesage.getTaskId();
			TaskId taskId = clientTaskIdMap.remove(clientTaskId);
			taskIdMap.remove(taskId);
			if (taskId == null) {
				return null;
			}
			callback.taskCancelled(taskId);
			return null;
		}
	}
}
