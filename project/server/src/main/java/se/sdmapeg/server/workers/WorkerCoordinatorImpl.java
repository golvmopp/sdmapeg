package se.sdmapeg.server.workers;

import se.sdmapeg.server.workers.controllers.WorkerConnectionCallback;
import se.sdmapeg.server.workers.callbacks.WorkerCoordinatorListenerSupport;
import se.sdmapeg.server.workers.models.Worker;
import se.sdmapeg.server.workers.models.WorkerCoordinatorState;
import se.sdmapeg.server.workers.models.WorkerCoordinatorModel;
import se.sdmapeg.server.workers.callbacks.WorkerCoordinatorCallback;
import se.sdmapeg.server.workers.callbacks.WorkerCoordinatorListener;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.server.communication.ConnectionAcceptor;
import se.sdmapeg.server.communication.ConnectionHandler;
import se.sdmapeg.serverworker.TaskId;
import se.sdmapeg.serverworker.communication.ServerToWorkerMessage;
import se.sdmapeg.serverworker.communication.WorkerToServerMessage;

/**
 * Implementation of a WorkerCoordinator.
 */
public final class WorkerCoordinatorImpl implements WorkerCoordinator {
	private static final Logger LOG = LoggerFactory.getLogger(WorkerCoordinatorImpl.class);
	private final ExecutorService connectionThreadPool;
	private final ConnectionHandler<ServerToWorkerMessage,
			WorkerToServerMessage> connectionHandler;
	private final WorkerCoordinatorModel state;
	private final AtomicBoolean started = new AtomicBoolean(false);

	private WorkerCoordinatorImpl(ExecutorService connectionThreadPool,
			ConnectionHandler<ServerToWorkerMessage,
					WorkerToServerMessage> connectionHandler,
			WorkerCoordinatorCallback callback,
			Executor listenerExecutor) {
		this.connectionThreadPool = connectionThreadPool;
		this.connectionHandler = connectionHandler;
		this.state = new WorkerCoordinatorModel(
			WorkerCoordinatorListenerSupport.newListenerSupport(
				listenerExecutor), callback);
	}

	@Override
	public void handleTask(TaskId taskId, Task<?> task) {
		state.handleTask(taskId, task);
	}

	@Override
	public void cancelTask(TaskId taskId) {
		state.cancelTask(taskId);
	}

	@Override
	public void shutDown() {
		try {
			/*
			 * Closes the connection handler. This will be noticed by the
			 * connection acceptor thread which will handle the rest of the
			 * shutdown work.
			 */
			LOG.info("Worker Coordinator Stopping");
			connectionHandler.close();
		} catch (IOException ex) {
			LOG.warn("An error occurred while closing the connection handler",
				ex);
		}
	}

	@Override
	public void disconnectWorker(InetSocketAddress workerAddress) {
		Worker worker = state.getWorker(workerAddress);
		if (worker != null) {
			worker.disconnect();
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
				connectionHandler, new WorkerConnectionCallback(state,
					connectionThreadPool));
			LOG.info("Worker Coordinator Started.");
		}
	}

	@Override
	public WorkerCoordinatorState getState() {
		if (isStopped()) {
			return WorkerCoordinatorState.STOPPED;
		} else if (isStaretd()) {
			return WorkerCoordinatorState.STARTED;
		} else {
			return WorkerCoordinatorState.CREATED;
		}
	}

	private boolean isStopped() {
		return !connectionHandler.isOpen();
	}

	private boolean isStaretd() {
		return started.get();
	}

	@Override
	public void addListener(WorkerCoordinatorListener listener) {
		state.addListener(listener);
	}

	@Override
	public void removeListener(WorkerCoordinatorListener listener) {
		state.removeListener(listener);
	}

	/**
	 * Creates a new WorkerCoordinator with the specified connectionThreadPool,
	 * connectionHandler, callback, and listener executor.
	 *
	 * @param connectionThreadPool a thread pool for handling connections
	 * @param connectionHandler a connection handler for dealing with new
	 *                          connections
	 * @param callback a callback to be notified of events
	 * @param listenerExecutor an Executor to be used for notifying listeners
	 * @return the created ClientManager
	 */
	public static WorkerCoordinator newWorkerCoordinator(
			ExecutorService connectionThreadPool,
			ConnectionHandler<ServerToWorkerMessage, WorkerToServerMessage>
				connectionHandler,
			WorkerCoordinatorCallback callback,
			Executor listenerExecutor) {
		return new WorkerCoordinatorImpl(connectionThreadPool,
			connectionHandler, callback, listenerExecutor);
	}
}
