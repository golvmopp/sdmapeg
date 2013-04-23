package se.sdmapeg.common.communication;

/**
 * Exception thrown to indicate that a network communication error occurred.
 */
public final class CommunicationException extends Exception {
	public CommunicationException() {
	}

	public CommunicationException(String message) {
		super(message);
	}

	public CommunicationException(String message, Throwable cause) {
		super(message, cause);
	}

	public CommunicationException(Throwable cause) {
		super(cause);
	}
}
