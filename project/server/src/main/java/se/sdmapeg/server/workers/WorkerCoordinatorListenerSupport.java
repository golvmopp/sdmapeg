package se.sdmapeg.server.workers;

import java.net.InetAddress;
import java.util.concurrent.Executors;
import se.sdmapeg.common.listeners.Listenable;
import se.sdmapeg.common.listeners.ListenerSupport;
import se.sdmapeg.common.listeners.Notification;
import se.sdmapeg.serverworker.TaskId;

/**
 * Class for providing support for WorkerCoordinatorListeners.
 */
final class WorkerCoordinatorListenerSupport implements WorkerCoordinatorListener,
		Listenable<WorkerCoordinatorListener> {
	private final ListenerSupport<WorkerCoordinatorListener> listenerSupport =
		ListenerSupport.newListenerSupport(Executors.newSingleThreadExecutor());

	private WorkerCoordinatorListenerSupport() {
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
	 * Returns a new WorkerCoordinatorListenerSupport.
	 *
	 * @return the newly created WorkerCoordinatorListenerSupport
	 */
	public static WorkerCoordinatorListenerSupport newListenerSupport() {
		return new WorkerCoordinatorListenerSupport();
	} 
}
