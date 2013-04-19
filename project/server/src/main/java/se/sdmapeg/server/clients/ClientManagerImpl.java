package se.sdmapeg.server.clients;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sdmapeg.common.IdGenerator;
import se.sdmapeg.common.communication.Connection;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.server.communication.ConnectionAcceptor;
import se.sdmapeg.server.communication.ConnectionHandler;
import se.sdmapeg.serverclient.communication.ClientToServerMessage;
import se.sdmapeg.serverclient.communication.ServerToClientMessage;
import se.sdmapeg.serverworker.TaskId;

/**
 *
 * @author niclas
 */
public class ClientManagerImpl implements ClientManager {
	private static final Logger LOG = LoggerFactory.getLogger(ClientManagerImpl.class);
	private final ExecutorService connectionThreadPool;
	private final ConnectionHandler<ServerToClientMessage,
			ClientToServerMessage> connectionHandler;
	private final IdGenerator<TaskId> taskIdGenerator;
	private final ClientManagerCallback callback;
	private final ClientCallback clientCallback =
		new ClientEventCallback();
	private final Map<TaskId, Client> taskMap =
		new ConcurrentHashMap<>();
	private final Map<InetAddress, Client> addressMap =
		new ConcurrentHashMap<>();
	private final AtomicBoolean started = new AtomicBoolean(false);

	public ClientManagerImpl(ExecutorService connectionThreadPool,
			ConnectionHandler<ServerToClientMessage, ClientToServerMessage>
				connectionHandler, IdGenerator<TaskId> taskIdGenerator,
			ClientManagerCallback callback) {
		this.connectionThreadPool = connectionThreadPool;
		this.connectionHandler = connectionHandler;
		this.taskIdGenerator = taskIdGenerator;
		this.callback = callback;
	}

	@Override
	public void handleResult(TaskId taskId, Result<?> result) {
		Client client = taskMap.remove(taskId);
		if (client == null) {
			return;
		}
		client.taskCompleted(taskId, result);
		LOG.info("Result for Task {} sent to {}", taskId, client);
	}

	@Override
	public void shutDown() {
		try {
			connectionHandler.close();
		} catch (IOException ex) {
			LOG.warn("An error occurred while closing the connection handler",
					 ex);
		}
	}

	@Override
	public void disconnectClient(InetAddress clientAddress) {
		Client client = addressMap.get(clientAddress);
		if (client == null) {
			return;
		}
		client.disconnect();
	}

	@Override
	public void start() {
		if (started.compareAndSet(false, true)) {
			connectionThreadPool.submit(new ConnectionAcceptor(
					connectionHandler, new ConnectionAcceptorCallback()));
			LOG.info("Client Manager Started");
		}
	}

	@Override
	public ClientManager.State getState() {
		if (isStopped()) {
			return ClientManager.State.STOPPED;
		} else if (isStarted()) {
			return ClientManager.State.STARTED;
		} else {
			return ClientManager.State.CREATED;
		}
	}

	private void clientDisconnected(Client client) {
		addressMap.remove(client.getAddress());
		for (TaskId task : client.getActiveTasks()) {
			cancelTask(task);
		}
	}

	private void connectionHandlerClosed() {
		for (Client client : addressMap.values()) {
			client.disconnect();
		}
		LOG.info("Client Manager Stopped");
	}

	private boolean isStopped() {
		return !connectionHandler.isOpen();
	}

	private boolean isStarted() {
		return started.get();
	}

	private void cancelTask(TaskId task) {
		taskMap.remove(task);
		callback.cancelTask(task);
	}

	private final class ConnectionAcceptorCallback
		implements ConnectionAcceptor.Callback<ServerToClientMessage,
			ClientToServerMessage> {
		@Override
		public void connectionReceived(Connection<ServerToClientMessage,
				ClientToServerMessage> connection) {
			Client client = new ClientImpl(connection, taskIdGenerator,
					clientCallback);
			LOG.info("{} connected", client);
			addressMap.put(client.getAddress(), client);
			connectionThreadPool.submit(new ClientListener(client));
		}

		@Override
		public void connectionHandlerClosed() {
			ClientManagerImpl.this.connectionHandlerClosed();
		}
	}

	private final class ClientListener implements Runnable {
		private final Client client;

		public ClientListener(Client client) {
			this.client = client;
		}

		@Override
		public void run() {
			client.listen();
		}
	}

	private final class ClientEventCallback implements ClientCallback {

		@Override
		public void taskReceived(Client client, TaskId taskId,
				Task<?> task) {
			taskMap.put(taskId, client);
			LOG.info("Task {} received from {}", taskId, client);
			callback.handleTask(taskId, task);
		}

		@Override
		public void taskCancelled(Client client, TaskId taskId) {
			LOG.info("Task {} cancelled by {}", taskId, client);
			cancelTask(taskId);
		}

		@Override
		public void clientDisconnected(Client client) {
			ClientManagerImpl.this.clientDisconnected(client);
		}	
	}
}