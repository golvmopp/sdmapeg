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
import se.sdmapeg.serverclient.communication.ClientIdentification;
import se.sdmapeg.serverclient.communication.ClientToServerMessage;
import se.sdmapeg.serverclient.communication.ResultMessage;
import se.sdmapeg.serverclient.communication.ServerToClientMessage;
import se.sdmapeg.serverclient.communication.TaskMessage;
import se.sdmapeg.serverworker.TaskId;

/**
 *
 * @author niclas
 */
class AlternateClient {
	private static final Logger LOG = LoggerFactory.getLogger(AlternateClient.class);
	private final Connection<ServerToClientMessage, ClientToServerMessage> connection;
	private final IdGenerator<TaskId> taskIdGenerator;
	private final Callback callback;
	private final MessageHandler messageHandler;
	private final Map<TaskId, ClientTaskId> tasksIdMap = new ConcurrentHashMap<>();

	public AlternateClient(Connection<ServerToClientMessage,
			ClientToServerMessage> connection,
							 IdGenerator<TaskId> taskIdGenerator,
							 Callback callback) {
		this.connection = connection;
		this.taskIdGenerator = taskIdGenerator;
		this.callback = callback;
		this.messageHandler = new MessageHandler();
	}

	public InetAddress getAddress() {
		return connection.getAddress();
	}

	public void listen() {
		try {
			while (!Thread.currentThread().isInterrupted()) {
				ClientToServerMessage message = connection.receive();
				handleMessage(message);
			}
		} catch (ConnectionClosedException ex) {
			LOG.info("{} disconnected", this);
		} catch (CommunicationException ex) {
			LOG.error("An error occurred while listening for messages", ex);
		} finally {
			disconnect();
			callback.clientDisconnected(AlternateClient.this);
		}
	}

	private void handleMessage(ClientToServerMessage message) {
		message.accept(messageHandler);
	}

	public void taskCompleted(TaskId taskId, Result<?> result) {
		send(ResultMessage.newResultMessage(tasksIdMap.get(taskId), result));
		tasksIdMap.remove(taskId);
	}

	public Set<TaskId> getActiveTasks() {
		return Collections.unmodifiableSet(new HashSet<>(tasksIdMap.keySet()));
	}

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

	public interface Callback {
		void taskReceived(AlternateClient client, TaskId taskId, Task<?> task);
		void clientDisconnected(AlternateClient client);
	}

	private final class MessageHandler
			implements ClientToServerMessage.Handler<Void> {

		@Override
		public Void handle(ClientIdentification message) {
			LOG.error("Received unexpected message type");
			disconnect();
			callback.clientDisconnected(AlternateClient.this);
			return null;
		}

		@Override
		public Void handle(TaskMessage message) {
			Task<?> task = message.getTask();
			ClientTaskId clientTaskId = message.getTaskId();
			TaskId taskId = taskIdGenerator.newId();
			tasksIdMap.put(taskId, clientTaskId);
			callback.taskReceived(AlternateClient.this, taskId, task);
			return null;
		}
	}
}
