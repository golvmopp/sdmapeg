package se.sdmapeg.server.clients;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sdmapeg.common.IdGenerator;
import se.sdmapeg.common.communication.Connection;
import se.sdmapeg.common.listeners.Listenable;
import se.sdmapeg.common.listeners.ListenerSupport;
import se.sdmapeg.common.listeners.Notification;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.server.communication.ConnectionAcceptor;
import se.sdmapeg.server.communication.ConnectionAcceptorCallback;
import se.sdmapeg.server.communication.ConnectionHandler;
import se.sdmapeg.serverclient.communication.ClientToServerMessage;
import se.sdmapeg.serverclient.communication.ServerToClientMessage;
import se.sdmapeg.serverworker.TaskId;

/**
 *
 * @author niclas
 */
public final class ClientManagerImpl implements ClientManager {
	private static final Logger LOG = LoggerFactory.getLogger(ClientManagerImpl.class);
	private final Listeners listeners = new Listeners();
	private final ExecutorService connectionThreadPool;
	private final ConnectionHandler<ServerToClientMessage,
			ClientToServerMessage> connectionHandler;
	private final IdGenerator<TaskId> taskIdGenerator;
	private final ClientManagerCallback callback;
	private final Map<TaskId, Client> taskMap =
		new ConcurrentHashMap<>();
	private final ConcurrentMap<InetAddress, Client> addressMap =
		new ConcurrentHashMap<>();
	private final AtomicBoolean started = new AtomicBoolean(false);

	private ClientManagerImpl(ExecutorService connectionThreadPool,
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
		listeners.resultSent(taskId, client.getAddress());
	}

	@Override
	public void shutDown() {
		try {
			/*
			 * Closes the connection handler. This will be noticed by the
			 * connection acceptor thread which will handle the rest of the
			 * shutdown work.
			 */
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
		disconnectClient(client);
	}

	@Override
	public void start() {
		/*
		 * compareAndSet to ensure that the connection acceptor thread will only
		 * be started the first time this method is called.
		 */
		if (started.compareAndSet(false, true)) {
			// Start a new thread to deal with incoming connections
			ConnectionAcceptor.acceptConnections(connectionThreadPool,
				connectionHandler, new ClientConnectionCallback());
			LOG.info("Client Manager Started");
		}
	}

	@Override
	public ClientManagerState getState() {
		if (isStopped()) {
			return ClientManagerState.STOPPED;
		} else if (isStarted()) {
			return ClientManagerState.STARTED;
		} else {
			return ClientManagerState.CREATED;
		}
	}

	private boolean isStopped() {
		return !connectionHandler.isOpen();
	}

	private boolean isStarted() {
		return started.get();
	}

	private void cancelTask(TaskId task) {
		Client client = taskMap.remove(task);
		listeners.taskCancelled(task, client.getAddress());
		callback.cancelTask(task);
	}

	private void disconnectClient(Client client) {
		/*
		 * Disconnects the client. This will be noticed by the client listener
		 * thread, which will handle the rest of the cleanup work.
		 */
		client.disconnect();
	}

	@Override
	public void addListener(ClientManagerListener listener) {
		listeners.addListener(listener);
	}

	@Override
	public void removeListener(ClientManagerListener listener) {
		listeners.removeListener(listener);
	}

	/**
	 * Creates a new ClientManager with the specified connectionThreadPool,
	 * connectionHandler, taskIdGenerator and callback.
	 *
	 * @param connectionThreadPool a thread pool for handling connections
	 * @param connectionHandler a connection handler for dealing with new
	 *							connections
	 * @param taskIdGenerator an IdGenerator for generating TaskIds
	 * @param callback a callback to be notified of events
	 * @return the created ClientManager
	 */
	public static ClientManager newClientManager(
			ExecutorService connectionThreadPool,
			ConnectionHandler<ServerToClientMessage, ClientToServerMessage>
				connectionHandler,
			IdGenerator<TaskId> taskIdGenerator,
			ClientManagerCallback callback) {
		return new ClientManagerImpl(connectionThreadPool, connectionHandler,
									 taskIdGenerator, callback);
	}

	private final class ClientConnectionCallback
		implements ConnectionAcceptorCallback<ServerToClientMessage,
			ClientToServerMessage> {
		@Override
		public void connectionReceived(Connection<ServerToClientMessage,
				ClientToServerMessage> connection) {
			Client client = ClientImpl.newClient(connection, taskIdGenerator);
			if (addressMap.putIfAbsent(client.getAddress(), client) == null) {
				LOG.info("{} connected", client);
				listeners.clientConnected(client.getAddress());
				// Start a new thread to listen to the client
				connectionThreadPool.submit(new ClientListener(client));
			} else {
				LOG.warn("Connection refused: {} attempted to connect, but was"
						+ " already connected", client);
				client.disconnect();
			}
		}

		@Override
		public void connectionHandlerClosed() {
			/*
			 * Disconnect all currently connected clients. Since this method is
			 * called by the only thread responsible for accepting new
			 * connections, we can safely assume that the collection will
			 * remain up to date without having to worry about new clients being
			 * added concurrently.
			 */
			for (Client client : addressMap.values()) {
				disconnectClient(client);
			}
			LOG.info("Client Manager Stopped");
		}
	}

	private final class ClientListener implements Runnable {
		private final Client client;

		public ClientListener(Client client) {
			this.client = client;
		}

		@Override
		public void run() {
			client.listen(new ClientEventCallback(client));
		}
	}

	private final class ClientEventCallback implements ClientCallback {
		private final Client client;

		public ClientEventCallback(Client client) {
			this.client = client;
		}

		@Override
		public void taskReceived(TaskId taskId, Task<?> task) {
			taskMap.put(taskId, client);
			LOG.info("Task {} received from {}", taskId, client);
			listeners.taskCancelled(taskId, client.getAddress());
			callback.handleTask(taskId, task);
		}

		@Override
		public void taskCancelled(TaskId taskId) {
			LOG.info("Task {} cancelled by {}", taskId, client);
			cancelTask(taskId);
		}

		@Override
		public void clientDisconnected() {
			addressMap.remove(client.getAddress());
			listeners.clientDisconnected(client.getAddress());
			/*
			 * Since this method is called by the only thread responsible for
			 * accepting new tasks from the client, we can be sure that the set
			 * of active tasks will be up to date and won't change any more.
			 * This allows us to simply iterate over it and cancel each task,
			 * without having to worry about any new tasks being added
			 * concurrently.
			 */
			for (TaskId task : client.getActiveTasks()) {
				LOG.info("Cancelling task {}", task);
				cancelTask(task);
			}
		}
	}

	private static final class Listeners implements ClientManagerListener,
			Listenable<ClientManagerListener> {
		private final ListenerSupport<ClientManagerListener> listenerSupport =
			ListenerSupport.newListenerSupport(
				Executors.newSingleThreadExecutor());

		@Override
		public void clientConnected(final InetAddress clientAddress) {
			listenerSupport.notifyListeners(
					new Notification<ClientManagerListener>() {
				@Override
				public void notifyListener(ClientManagerListener listener) {
					listener.clientConnected(null);
				}
			});
		}

		@Override
		public void clientDisconnected(final InetAddress clientAddress) {
			listenerSupport.notifyListeners(
					new Notification<ClientManagerListener>() {
				@Override
				public void notifyListener(ClientManagerListener listener) {
					listener.clientDisconnected(clientAddress);
				}
			});
		}

		@Override
		public void taskReceived(final TaskId taskId, final InetAddress address) {
			listenerSupport.notifyListeners(
					new Notification<ClientManagerListener>() {
				@Override
				public void notifyListener(ClientManagerListener listener) {
					listener.taskReceived(taskId, address);
				}
			});
		}

		@Override
		public void taskCancelled(final TaskId taskId, final InetAddress address) {
			listenerSupport.notifyListeners(
					new Notification<ClientManagerListener>() {
				@Override
				public void notifyListener(ClientManagerListener listener) {
					listener.taskCancelled(taskId, address);
				}
			});
		}

		@Override
		public void resultSent(final TaskId taskId, final InetAddress address) {
			listenerSupport.notifyListeners(
					new Notification<ClientManagerListener>() {
				@Override
				public void notifyListener(ClientManagerListener listener) {
					listener.resultSent(taskId, address);
				}
			});
		}

		@Override
		public void addListener(ClientManagerListener listener) {
			listenerSupport.addListener(listener);
		}

		@Override
		public void removeListener(ClientManagerListener listener) {
			listenerSupport.removeListener(listener);
		}
	}
}