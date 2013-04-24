package se.sdmapeg.common.listeners;

/**
 * Interface representing a notification to a listener.
 *
 * @param <L> the type of listener that this notification applies to
 */
public interface Notification<L> {

	/**
	 * Notifies the specified listener of the event represented by this
	 * notification.
	 *
	 * @param listener the listener to notify
	 */
	void notifyListener(L listener);
}
