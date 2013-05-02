package se.sdmapeg.server.clients.callbacks;

import java.net.InetAddress;
import java.util.concurrent.Executor;
import se.sdmapeg.common.listeners.Listenable;
import se.sdmapeg.common.listeners.ListenerSupport;
import se.sdmapeg.common.listeners.Notification;
import se.sdmapeg.serverworker.TaskId;

/**
 * Class for providing support for ClientManagerListeners.
 */
public final class ClientManagerListenerSupport implements ClientManagerListener,
		Listenable<ClientManagerListener> {
	private final ListenerSupport<ClientManagerListener> listenerSupport;

	private ClientManagerListenerSupport(Executor notificationExecutor) {
		listenerSupport =
			ListenerSupport.newListenerSupport(notificationExecutor);
	}

	@Override
	public void clientConnected(final InetAddress address) {
		listenerSupport.notifyListeners(
				new Notification<ClientManagerListener>() {
			@Override
			public void notifyListener(ClientManagerListener listener) {
				listener.clientConnected(null);
			}
		});
	}

	@Override
	public void clientDisconnected(final InetAddress address) {
		listenerSupport.notifyListeners(
				new Notification<ClientManagerListener>() {
			@Override
			public void notifyListener(ClientManagerListener listener) {
				listener.clientDisconnected(address);
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

	/**
	 * Returns a new ClientManagerListenerSupport using the specified Executor
	 * to notify its listeners of events.
	 *
	 * @param notificationExecutor	an executor to be used for performing
	 *								notifications
	 * @return the newly created ClientManagerListenerSupport
	 */
	public static ClientManagerListenerSupport newListenerSupport(
			Executor notificationExecutor) {
		return new ClientManagerListenerSupport(notificationExecutor);
	}
}
