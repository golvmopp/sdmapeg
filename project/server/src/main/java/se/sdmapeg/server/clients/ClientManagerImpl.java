package se.sdmapeg.server.clients;

import se.sdmapeg.server.clients.models.ClientManagerModel;
import se.sdmapeg.server.clients.models.ClientManagerState;
import se.sdmapeg.server.clients.models.Client;
import se.sdmapeg.server.clients.controllers.ClientConnectionCallback;
import se.sdmapeg.server.clients.callbacks.ClientManagerListenerSupport;
import se.sdmapeg.server.clients.callbacks.ClientManagerListener;
import se.sdmapeg.server.clients.callbacks.ClientManagerCallback;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sdmapeg.common.IdGenerator;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.server.communication.ConnectionAcceptor;
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
	private final ClientManagerModel state;
	private final ExecutorService connectionThreadPool;
	private final ConnectionHandler<ServerToClientMessage,
			ClientToServerMessage> connectionHandler;
	private final IdGenerator<TaskId> taskIdGenerator;
	private final AtomicBoolean started = new AtomicBoolean(false);

	private ClientManagerImpl(ExecutorService connectionThreadPool,
			ConnectionHandler<ServerToClientMessage, ClientToServerMessage>
				connectionHandler, IdGenerator<TaskId> taskIdGenerator,
			ClientManagerCallback callback, Executor listenerExecutor) {
		this.state = new ClientManagerModel(
			ClientManagerListenerSupport.newListenerSupport(listenerExecutor),
			callback);
		this.connectionThreadPool = connectionThreadPool;
		this.connectionHandler = connectionHandler;
		this.taskIdGenerator = taskIdGenerator;
	}

	@Override
	public void handleResult(TaskId taskId, Result<?> result) {
		state.handleResult(taskId, result);
	}

	@Override
	public void shutDown() {
		try {
			/*
			 * Closes the connection handler. This will be noticed by the
			 * connection acceptor thread which will handle the rest of the
			 * shutdown work.
			 */
			LOG.info("Client Manager Stopping");
			connectionHandler.close();
		} catch (IOException ex) {
			LOG.warn("An error occurred while closing the connection handler",
				ex);
		}
	}

	@Override
	public void disconnectClient(InetSocketAddress clientAddress) {
		Client client = state.getClient(clientAddress);
		if (client != null) {
			client.disconnect();
		}
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
				connectionHandler, new ClientConnectionCallback(
					state, connectionThreadPool, taskIdGenerator));
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

	@Override
	public void addListener(ClientManagerListener listener) {
		state.addListener(listener);
	}

	@Override
	public void removeListener(ClientManagerListener listener) {
		state.removeListener(listener);
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
	 * @param listenerExecutor an Executor to be used for notifying listeners
	 * @return the created ClientManager
	 */
	public static ClientManager newClientManager(
			ExecutorService connectionThreadPool,
			ConnectionHandler<ServerToClientMessage, ClientToServerMessage>
				connectionHandler,
			IdGenerator<TaskId> taskIdGenerator,
			ClientManagerCallback callback,
			Executor listenerExecutor) {
		return new ClientManagerImpl(connectionThreadPool, connectionHandler,
				taskIdGenerator, callback, listenerExecutor);
	}
}