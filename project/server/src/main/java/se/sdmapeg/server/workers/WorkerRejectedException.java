package se.sdmapeg.server.workers;

/**
 * Exception thrown to indicate that a worker was rejected for some reason.
 */
class WorkerRejectedException extends Exception {
	private static final long serialVersionUID = 1L;

	public WorkerRejectedException() {
	}

	public WorkerRejectedException(String message) {
		super(message);
	}

	public WorkerRejectedException(Throwable cause) {
		super(cause);
	}

	public WorkerRejectedException(String message, Throwable cause) {
		super(message, cause);
	}	
}
