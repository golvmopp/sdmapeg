package se.sdmapeg.server.workers.exceptions;

/**
 * Exception thrown to indicate that a task was rejected for some reason.
 */
public final class TaskRejectedException extends Exception {
	private static final long serialVersionUID = 1L;

	public TaskRejectedException() {
	}

	public TaskRejectedException(String message) {
		super(message);
	}

	public TaskRejectedException(Throwable cause) {
		super(cause);
	}

	public TaskRejectedException(String message, Throwable cause) {
		super(message, cause);
	}
}
