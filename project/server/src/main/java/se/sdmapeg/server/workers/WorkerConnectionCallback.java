package se.sdmapeg.server.workers;

import java.util.concurrent.ExecutorService;
import se.sdmapeg.common.communication.Connection;
import se.sdmapeg.server.communication.ConnectionAcceptorCallback;
import se.sdmapeg.serverworker.communication.ServerToWorkerMessage;
import se.sdmapeg.serverworker.communication.WorkerToServerMessage;

/**
 * Callback for taking care of connections from workers.
 */
final class WorkerConnectionCallback implements
		ConnectionAcceptorCallback<ServerToWorkerMessage, WorkerToServerMessage> {
	private final WorkerCoordinatorModel state;
	private final ExecutorService connectionThreadPool;

	public WorkerConnectionCallback(WorkerCoordinatorModel state,
									ExecutorService connectionThreadPool) {
		this.state = state;
		this.connectionThreadPool = connectionThreadPool;
	}

	@Override
	public void connectionReceived(Connection<ServerToWorkerMessage,
			WorkerToServerMessage> connection) {
		final Worker worker = WorkerImpl.newWorker(connection);
		try {
			state.addWorker(worker);
			connectionThreadPool.submit(new Runnable() {
				@Override
				public void run() {
					worker.listen(new WorkerEventCallback(state, worker));
				}
			});
		}
		catch (WorkerRejectedException ex) {
			worker.disconnect();
		}
	}

	@Override
	public void connectionHandlerClosed() {
		/*
		 * Disconnect all currently connected workers. Since this method is
		 * called by the only thread responsible for accepting new
		 * connections, we can safely assume that the collection will
		 * remain up to date without having to worry about new workers being
		 * added concurrently.
		 */
		for (Worker worker : state.getWorkers()) {
			worker.disconnect();
		}
	}
}
