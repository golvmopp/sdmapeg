package se.sdmapeg.server.workers.models;

/**
 * Exception thrown to indicate that there are no workers available for
 * performing a task.
 */
public class NoWorkersAvailableException extends Exception {
	private static final long serialVersionUID = 1L;
	/**
	 * Creates a new instance of
	 * <code>NoWorkerAvailableException</code> without detail message.
	 */
	public NoWorkersAvailableException() {
	}

	/**
	 * Constructs an instance of
	 * <code>NoWorkerAvailableException</code> with the specified detail
	 * message.
	 *
	 * @param msg the detail message.
	 */
	public NoWorkersAvailableException(String msg) {
		super(msg);
	}

	public NoWorkersAvailableException(Throwable cause) {
		super(cause);
	}

	public NoWorkersAvailableException(String message, Throwable cause) {
		super(message, cause);
	}
}
