package se.sdmapeg.common.listeners;

/**
 * Interface for representing a class that can be listened to using listeners of
 * a specific type.
 *
 * @param <L> the type of listener that this Listenable supports
 */
public interface Listenable<L> {

	/**
	 * Adds the specified listener to the set of listeners to be notified when
	 * events occur.
	 *
	 * @param listener the listener to be added
	 */
	void addListener(L listener);

	/**
	 * Removes the specified listener from the set of listeners to be notified
	 * when events occur.
	 *
	 * @param listener the listener to be removed
	 */
	void removeListener(L listener);
	
}
