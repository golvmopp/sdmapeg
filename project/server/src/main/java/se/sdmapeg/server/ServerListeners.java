package se.sdmapeg.server;

import java.net.InetAddress;
import se.sdmapeg.server.clients.ClientManagerListener;
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
		public void clientConnected(InetAddress address) {
			serverListener.clientConnected(address);
		}

		@Override
		public void clientDisconnected(InetAddress address) {
			serverListener.clientDisconnected(address);
		}

		@Override
		public void taskReceived(TaskId taskId, InetAddress address) {
			serverListener.taskReceivedFromClient(taskId, address);
		}

		@Override
		public void taskCancelled(TaskId taskId, InetAddress address) {
			serverListener.taskCancelledByClient(taskId, address);
		}

		@Override
		public void resultSent(TaskId taskId, InetAddress address) {
			serverListener.resultSentToClient(taskId, address);
		}

		@Override
		public void workerConnected(InetAddress address) {
			serverListener.workerConnected(address);
		}

		@Override
		public void workerDisconnected(InetAddress address) {
			serverListener.workerDisconnected(address);
		}

		@Override
		public void resultReceived(TaskId taskId, InetAddress address) {
			serverListener.resultReceivedFromWorker(taskId, address);
		}

		@Override
		public void taskAssigned(TaskId taskId, InetAddress address) {
			serverListener.taskAssignedToWorker(taskId, address);
		}

		@Override
		public void taskAborted(TaskId taskId, InetAddress address) {
			serverListener.taskAbortedByWorker(taskId, address);
		}
	}
}
