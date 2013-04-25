package se.sdmapeg.server.workers;

import java.net.InetAddress;
import java.util.concurrent.Executor;
import se.sdmapeg.common.listeners.Listenable;
import se.sdmapeg.common.listeners.ListenerSupport;
import se.sdmapeg.common.listeners.Notification;
import se.sdmapeg.serverworker.TaskId;

/**
 * Class for providing support for WorkerCoordinatorListeners.
 */
final class WorkerCoordinatorListenerSupport implements WorkerCoordinatorListener,
		Listenable<WorkerCoordinatorListener> {
	private final ListenerSupport<WorkerCoordinatorListener> listenerSupport;

	private WorkerCoordinatorListenerSupport(Executor notificationExecutor) {
		listenerSupport =
			ListenerSupport.newListenerSupport(notificationExecutor);
	}

	@Override
	public void workerConnected(final InetAddress address) {
		listenerSupport.notifyListeners(
				new Notification<WorkerCoordinatorListener>() {
			@Override
			public void notifyListener(WorkerCoordinatorListener listener) {
				listener.workerConnected(address);
			}
		});
	}

	@Override
	public void workerDisconnected(final InetAddress address) {
		listenerSupport.notifyListeners(
				new Notification<WorkerCoordinatorListener>() {
			@Override
			public void notifyListener(WorkerCoordinatorListener listener) {
				listener.workerDisconnected(address);
			}
		});
	}

	@Override
	public void resultReceived(final TaskId taskId, final InetAddress address) {
		listenerSupport.notifyListeners(
				new Notification<WorkerCoordinatorListener>() {
			@Override
			public void notifyListener(WorkerCoordinatorListener listener) {
				listener.resultReceived(taskId, address);
			}
		});
	}

	@Override
	public void taskAssigned(final TaskId taskId, final InetAddress address) {
		listenerSupport.notifyListeners(
				new Notification<WorkerCoordinatorListener>() {
			@Override
			public void notifyListener(WorkerCoordinatorListener listener) {
				listener.taskAssigned(taskId, address);
			}
		});
	}

	@Override
	public void taskAborted(final TaskId taskId, final InetAddress address) {
		listenerSupport.notifyListeners(
				new Notification<WorkerCoordinatorListener>() {
			@Override
			public void notifyListener(WorkerCoordinatorListener listener) {
				listener.taskAborted(taskId, address);
			}
		});
	}

	@Override
	public void addListener(WorkerCoordinatorListener listener) {
		listenerSupport.addListener(listener);
	}

	@Override
	public void removeListener(WorkerCoordinatorListener listener) {
		listenerSupport.removeListener(listener);
	}

	/**
	 * Returns a new WorkerCoordinatorListenerSupport using the specified
	 * Executor to notify its listeners of events.
	 *
	 * @param notificationExecutor	an executor to be used for performing
	 *								notifications
	 * @return the newly created WorkerCoordinatorListenerSupport
	 */
	public static WorkerCoordinatorListenerSupport newListenerSupport(
			Executor notificationExecutor) {
		return new WorkerCoordinatorListenerSupport(notificationExecutor);
	} 
}
