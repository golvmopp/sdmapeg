package se.sdmapeg.server.clients;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sdmapeg.common.IdGenerator;
import se.sdmapeg.common.communication.CommunicationException;
import se.sdmapeg.common.communication.Connection;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.server.communication.ConnectionHandler;
import se.sdmapeg.serverclient.communication.ClientToServerMessage;
import se.sdmapeg.serverclient.communication.ServerToClientMessage;
import se.sdmapeg.serverworker.TaskId;

/**
 *
 * @author niclas
 */
public class AlternateClientManager implements ClientManager {
	private static final Logger LOG = LoggerFactory.getLogger(AlternateClientManager.class);
	private final ExecutorService connectionThreadPool;
	private final ConnectionHandler<ServerToClientMessage,
			ClientToServerMessage> connectionHandler;
	private final IdGenerator<TaskId> taskIdGenerator;
	private final ClientManagerCallback callback;
	private final AlternateClient.Callback clientCallback =
		new ClientCallback();
	private final Map<TaskId, AlternateClient> taskMap =
		new ConcurrentHashMap<>();
	private final Map<InetAddress, AlternateClient> addressMap =
		new ConcurrentHashMap<>();
	private final AtomicBoolean started = new AtomicBoolean(false);

	public AlternateClientManager(ExecutorService connectionThreadPool,
			ConnectionHandler<ServerToClientMessage, ClientToServerMessage>
				connectionHandler, IdGenerator<TaskId> taskIdGenerator,
			ClientManagerCallback client) {
		this.connectionThreadPool = connectionThreadPool;
		this.connectionHandler = connectionHandler;
		this.taskIdGenerator = taskIdGenerator;
		this.callback = client;
	}

	@Override
	public void handleResult(TaskId taskId, Result<?> result) {
		AlternateClient client = taskMap.remove(taskId);
		if (client == null) {
			return;
		}
		client.taskCompleted(taskId, result);
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
		AlternateClient client = addressMap.remove(clientAddress);
		client.disconnect();
	}

	@Override
	public void start() {
		if (started.compareAndSet(false, true)) {
			connectionThreadPool.submit(new ConnectionAcceptor());
		}
	}

	@Override
	public State getState() {
		if (isStopped()) {
			return State.STOPPED;
		} else if (isStarted()) {
			return State.STARTED;
		} else {
			return State.CREATED;
		}
	}

	private void clientDisconnected(AlternateClient client) {
		addressMap.remove(client.getAddress());
		for (TaskId task : client.getActiveTasks()) {
			taskMap.remove(task);
			// TODO: Cancel task here
		}
	}

	private void connectionHandlerClosed() {
		for (AlternateClient client : addressMap.values()) {
			client.disconnect();
		}
	}

	private boolean isStopped() {
		return !connectionHandler.isOpen();
	}

	private boolean isStarted() {
		return started.get();
	}

	private final class ConnectionAcceptor implements Runnable {
		@Override
		public void run() {
			try {
				while (!Thread.currentThread().isInterrupted()) {
					Connection<ServerToClientMessage,
							ClientToServerMessage> connection =
						connectionHandler.accept();
					connectionThreadPool.submit(new ClientListener(
							new AlternateClient(connection, taskIdGenerator,
								clientCallback)));
				}
			} catch (SocketException ex) {
				// The connection handler was shut down.
			} catch (CommunicationException ex) {
				LOG.error("An error occurred while listening for connections",
						  ex);
			} finally {
				connectionHandlerClosed();
			}
		}
	}

	private final class ClientListener implements Runnable {
		private final AlternateClient client;

		public ClientListener(AlternateClient client) {
			this.client = client;
		}

		@Override
		public void run() {
			client.listen();
		}
	}

	private final class ClientCallback implements AlternateClient.Callback {

		@Override
		public void taskReceived(AlternateClient client, TaskId taskId,
				Task<?> task) {
			taskMap.put(taskId, client);
			callback.handleTask(taskId, task);
		}

		@Override
		public void clientDisconnected(AlternateClient client) {
			AlternateClientManager.this.clientDisconnected(client);
		}	
	}
}