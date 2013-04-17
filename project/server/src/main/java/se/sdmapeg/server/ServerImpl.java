package se.sdmapeg.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import se.sdmapeg.common.IdGenerator;
import se.sdmapeg.common.communication.CommunicationException;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.server.clients.ClientManager;
import se.sdmapeg.server.clients.ClientManagerCallback;
import se.sdmapeg.server.clients.ClientManagerImpl;
import se.sdmapeg.server.communication.ConnectionHandler;
import se.sdmapeg.server.communication.ConnectionHandlerImpl;
import se.sdmapeg.server.workers.WorkerCoordinator;
import se.sdmapeg.server.workers.WorkerCoordinatorCallback;
import se.sdmapeg.server.workers.WorkerCoordinatorImpl;
import se.sdmapeg.serverclient.communication.ClientToServerMessage;
import se.sdmapeg.serverclient.communication.ServerToClientMessage;
import se.sdmapeg.serverworker.TaskId;
import se.sdmapeg.serverworker.communication.ServerToWorkerMessage;
import se.sdmapeg.serverworker.communication.WorkerToServerMessage;

/**
 *
 * @author niclas
 */
public class ServerImpl implements Server {
	private static final int CLIENT_PORT = 666666;
	private static final int WORKER_PORT = 666667;
	private final ExecutorService connectionThreadPool =
		Executors.newCachedThreadPool();
	private final IdGenerator<TaskId> taskIdGenerator = null;
	private final ClientManager clientManager;
	private final WorkerCoordinator workerCoordinator;

	public ServerImpl() throws CommunicationException {
			ConnectionHandler<ServerToClientMessage,
					ClientToServerMessage> clientConnectionHandler;
			try {
				clientConnectionHandler =
					ConnectionHandlerImpl.newConnectionHandler(CLIENT_PORT);
			} catch (CommunicationException ex) {
				throw ex;
			}
			clientManager = new ClientManagerImpl(connectionThreadPool,
				clientConnectionHandler, taskIdGenerator, new ClientsCallback());
			ConnectionHandler<ServerToWorkerMessage, 
					WorkerToServerMessage> workerConnectionHandler;
			try {
				workerConnectionHandler =
					ConnectionHandlerImpl.newConnectionHandler(WORKER_PORT);
			} catch (CommunicationException ex) {
				clientManager.shutDown();
				throw ex;
			}
			workerCoordinator = new WorkerCoordinatorImpl(connectionThreadPool,
				workerConnectionHandler, new WorkersCallback());
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
