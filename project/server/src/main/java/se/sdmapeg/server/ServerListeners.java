package se.sdmapeg.server;

import java.net.InetSocketAddress;
import se.sdmapeg.server.clients.callbacks.ClientManagerListener;
import se.sdmapeg.server.workers.callbacks.WorkerCoordinatorListener;
import se.sdmapeg.serverworker.TaskId;

/**
 * Utility class for dealing with ServerListeners.
 */
final class ServerListeners {

	private ServerListeners() {
		// prevent instantiation
		throw new AssertionError();
	}

	/**
	 * Returns a ClientManagerListener forwarding its notifications to the
	 * specified delegate.
	 *
	 * @param delegate the ServerListener to forward notifications to
	 * @return a ClientManagerListener
	 */
	public static ClientManagerListener getClientManagerListener(
			ServerListener delegate) {
		return new DelegatingDualListener(delegate);
	}

	
	/**
	 * Returns a WorkerCoordinatorListener forwarding its notifications to the
	 * specified delegate.
	 *
	 * @param delegate the ServerListener to forward notifications to
	 * @return a WorkerCoordinatorListener
	 */
	public static WorkerCoordinatorListener getWorkerCoordinatorListener(
			ServerListener delegate) {
		return new DelegatingDualListener(delegate);
	}

	private static final class DelegatingDualListener
			implements ClientManagerListener, WorkerCoordinatorListener {
		private final ServerListener serverListener;

		public DelegatingDualListener(ServerListener serverListener) {
			this.serverListener = serverListener;
		}

		@Override
		public void clientConnected(InetSocketAddress address) {
			serverListener.clientConnected(address);
		}

		@Override
		public void clientDisconnected(InetSocketAddress address) {
			serverListener.clientDisconnected(address);
		}

		@Override
		public void taskReceived(TaskId taskId, InetSocketAddress address) {
			serverListener.taskReceivedFromClient(taskId, address);
		}

		@Override
		public void taskCancelled(TaskId taskId, InetSocketAddress address) {
			serverListener.taskCancelledByClient(taskId, address);
		}

		@Override
		public void resultSent(TaskId taskId, InetSocketAddress address) {
			serverListener.resultSentToClient(taskId, address);
		}

		@Override
		public void workerConnected(InetSocketAddress address) {
			serverListener.workerConnected(address);
		}

		@Override
		public void workerDisconnected(InetSocketAddress address) {
			serverListener.workerDisconnected(address);
		}

		@Override
		public void resultReceived(TaskId taskId, InetSocketAddress address) {
			serverListener.resultReceivedFromWorker(taskId, address);
		}

		@Override
		public void taskAssigned(TaskId taskId, InetSocketAddress address) {
			serverListener.taskAssignedToWorker(taskId, address);
		}

		@Override
		public void taskAborted(TaskId taskId, InetSocketAddress address) {
			serverListener.taskAbortedByWorker(taskId, address);
		}
	}
}
