package se.sdmapeg.server.clients;

import java.net.InetAddress;
import java.util.concurrent.Executors;
import se.sdmapeg.common.listeners.Listenable;
import se.sdmapeg.common.listeners.ListenerSupport;
import se.sdmapeg.common.listeners.Notification;
import se.sdmapeg.serverworker.TaskId;

/**
 * Class for providing support for ClientManagerListeners.
 */
final class ClientManagerListenerSupport implements ClientManagerListener,
		Listenable<ClientManagerListener> {
	private final ListenerSupport<ClientManagerListener> listenerSupport =
		ListenerSupport.newListenerSupport(Executors.newSingleThreadExecutor());

	private ClientManagerListenerSupport() {
	}

	@Override
	public void clientConnected(final InetAddress clientAddress) {
		listenerSupport.notifyListeners(
				new Notification<ClientManagerListener>() {
			@Override
			public void notifyListener(ClientManagerListener listener) {
				listener.clientConnected(null);
			}
		});
	}

	@Override
	public void clientDisconnected(final InetAddress clientAddress) {
		listenerSupport.notifyListeners(
				new Notification<ClientManagerListener>() {
			@Override
			public void notifyListener(ClientManagerListener listener) {
				listener.clientDisconnected(clientAddress);
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
	 * Returns a new ClientManagerListenerSupport.
	 *
	 * @return the newly created ClientManagerListenerSupport
	 */
	public static ClientManagerListenerSupport newListenerSupport() {
		return new ClientManagerListenerSupport();
	}
}
