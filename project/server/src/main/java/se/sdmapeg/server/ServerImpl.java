package se.sdmapeg.server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import se.sdmapeg.common.IdGenerator;
import se.sdmapeg.common.communication.CommunicationException;
import se.sdmapeg.common.listeners.Listenable;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.server.clients.ClientManager;
import se.sdmapeg.server.clients.ClientManagerCallback;
import se.sdmapeg.server.clients.ClientManagerImpl;
import se.sdmapeg.server.clients.ClientManagerListener;
import se.sdmapeg.server.communication.ConnectionHandler;
import se.sdmapeg.server.communication.ConnectionHandlerImpl;
import se.sdmapeg.server.workers.WorkerCoordinator;
import se.sdmapeg.server.workers.WorkerCoordinatorCallback;
import se.sdmapeg.server.workers.WorkerCoordinatorImpl;
import se.sdmapeg.server.workers.WorkerCoordinatorListener;
import se.sdmapeg.serverclient.communication.ClientToServerMessage;
import se.sdmapeg.serverclient.communication.ServerToClientMessage;
import se.sdmapeg.serverworker.TaskId;
import se.sdmapeg.serverworker.TaskIdGenerator;
import se.sdmapeg.serverworker.communication.ServerToWorkerMessage;
import se.sdmapeg.serverworker.communication.WorkerToServerMessage;

/**
 * Concrete implementation of a Server.
 */
public final class ServerImpl implements Server {
	private static final int CLIENT_PORT = 6666;
	private static final int WORKER_PORT = 6667;
	private final ConcurrentMap<ServerListener,
			ClientManagerListener> clientManagerListeners =
		new ConcurrentHashMap<>();
	private final ConcurrentMap<ServerListener,
			WorkerCoordinatorListener> workerCoordinatorListeners =
		new ConcurrentHashMap<>();
	private final ExecutorService connectionThreadPool =
		Executors.newCachedThreadPool();
	private final ExecutorService listenerExecutor =
		Executors.newSingleThreadExecutor();
	private final IdGenerator<TaskId> taskIdGenerator = new TaskIdGenerator();
	private final ClientManager clientManager;
	private final WorkerCoordinator workerCoordinator;

	private ServerImpl() throws CommunicationException {
		ConnectionHandler<ServerToClientMessage,
				ClientToServerMessage> clientConnectionHandler;
		try {
			clientConnectionHandler =
				ConnectionHandlerImpl.newConnectionHandler(CLIENT_PORT);
		} catch (CommunicationException ex) {
			throw ex;
		}
		clientManager = ClientManagerImpl.newClientManager(connectionThreadPool,
			clientConnectionHandler, taskIdGenerator, new ClientsCallback(),
			listenerExecutor);
		ConnectionHandler<ServerToWorkerMessage,
				WorkerToServerMessage> workerConnectionHandler;
		try {
			workerConnectionHandler =
				ConnectionHandlerImpl.newConnectionHandler(WORKER_PORT);
		} catch (CommunicationException ex) {
			clientManager.shutDown();
			throw ex;
		}
		workerCoordinator = WorkerCoordinatorImpl.newWorkerCoordinator(
				connectionThreadPool, workerConnectionHandler,
				new WorkersCallback(), listenerExecutor);
	}

	@Override
	public void start() {
		workerCoordinator.start();
		clientManager.start();
	}

	@Override
	public void shutDown() {
		clientManager.shutDown();
		workerCoordinator.shutDown();
		connectionThreadPool.shutdown();
		listenerExecutor.shutdown();
	}

	@Override
	public void addListener(ServerListener listener) {
		ClientManagerListener clientManagerListener =
			ServerListeners.getClientManagerListener(listener);
		mapListener(listener, clientManagerListener, clientManagerListeners,
			clientManager);
		WorkerCoordinatorListener workerCoordinatorListener =
			ServerListeners.getWorkerCoordinatorListener(listener);
		mapListener(listener, workerCoordinatorListener,
			workerCoordinatorListeners, workerCoordinator);
	}

	@Override
	public void removeListener(ServerListener listener) {
		unmapListener(listener, clientManagerListeners, clientManager);
		unmapListener(listener, workerCoordinatorListeners, workerCoordinator);
	}

	private static <L> void mapListener(ServerListener serverListener,
			L listener, ConcurrentMap<ServerListener, L> listenerMap,
			Listenable<L> listenable) {
		if (listenerMap.putIfAbsent(serverListener, listener) == null) {
			listenable.addListener(listener);
		}
	}

	private static <L> void unmapListener(ServerListener serverListener,
			ConcurrentMap<ServerListener, L> listenerMap,
			Listenable<L> listenable) {
		L listener = listenerMap.remove(serverListener);
		if (listener != null) {
			listenable.removeListener(listener);
		}
	}

	/**
	 * Creates a new Server.
	 *
	 * @throws	CommunicationException if something went wrong when creating the
	 *			client manager or worker coordinator
	 * @return the created Server
	 */
	public static Server newServer() throws CommunicationException {
		return new ServerImpl();
	}

	private final class ClientsCallback implements ClientManagerCallback {
		@Override
		public void handleTask(TaskId taskId, Task<?> task) {
			workerCoordinator.handleTask(taskId, task);
		}

		@Override
		public void cancelTask(TaskId taskId) {
			workerCoordinator.cancelTask(taskId);
		}		
	}

	private final class WorkersCallback implements WorkerCoordinatorCallback {
		@Override
		public void handleResult(TaskId taskId, Result<?> result) {
			clientManager.handleResult(taskId, result);
		}
	}
}
