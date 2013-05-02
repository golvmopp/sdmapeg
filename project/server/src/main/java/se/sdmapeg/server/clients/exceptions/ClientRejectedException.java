package se.sdmapeg.server.clients.exceptions;

/**
 * Exception thrown to indicate that a client has been rejected for some reason.
 */
public class ClientRejectedException extends Exception {
	private static final long serialVersionUID = 1L;

	public ClientRejectedException() {
	}

	public ClientRejectedException(String message) {
		super(message);
	}

	public ClientRejectedException(Throwable cause) {
		super(cause);
	}

	public ClientRejectedException(String message, Throwable cause) {
		super(message, cause);
	}	
}
