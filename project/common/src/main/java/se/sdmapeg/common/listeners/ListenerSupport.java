package se.sdmapeg.common.listeners;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;

/**
 * Class providing support for listeners and notifications.
 *
 * @param <L> the type of listener that this Listenable supports
 */
public final class ListenerSupport<L> implements Listenable<L> {
	private final Set<L> listeners = new CopyOnWriteArraySet<>();
	private final Executor notificationExecutor;

	private ListenerSupport(Executor notificationExecutor) {
		this.notificationExecutor = notificationExecutor;
	}

	@Override
	public void addListener(L listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(L listener) {
		listeners.remove(listener);
	}

	/**
	 * Notifies all listeners currently listening for events using the specified
	 * Notification.
	 *
	 * @param notification	the notification to be used for notifying the
	 *						listeners
	 */
	public void notifyListeners(final Notification<L> notification) {
		for (final L listener : listeners) {
			notificationExecutor.execute(new Notifier<>(notification, listener));
		}
	}

	private static class Notifier<L> implements Runnable {
		private final Notification<L> notification;
		private final L listener;

		public Notifier(Notification<L> notification, L listener) {
			this.notification = notification;
			this.listener = listener;
		}

		@Override
		public void run() {
			notification.notifyListener(listener);
		}
	}

	/**
	 * Creates a new ListenerSupport using the specified ExecutorService to run
	 * its notifications.
	 *
	 * @param <L>	the type of listeners to be supported by the created
	 *				ListenerSupport
	 * @param notificationExecutor	an executor to be used for performing
	 *								notifications
	 * @return the newly created ListenerSupport
	 */
	public static <L> ListenerSupport<L> newListenerSupport(
			Executor notificationExecutor) {
		return new ListenerSupport<>(notificationExecutor);
	}
}
