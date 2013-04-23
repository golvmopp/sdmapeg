package se.sdmapeg.common.communication;

/**
 * CommunicationException thrown to indicate that a connection was closed.
 */
public final class ConnectionClosedException extends CommunicationException {
	/**
	 * Creates a new instance of
	 * <code>ConnectionClosedException</code> without detail message.
	 */
	public ConnectionClosedException() {
	}

	/**
	 * Constructs an instance of
	 * <code>ConnectionClosedException</code> with the specified detail message.
	 *
	 * @param msg the detail message.
	 */
	public ConnectionClosedException(String msg) {
		super(msg);
	}

	public ConnectionClosedException(Throwable cause) {
		super(cause);
	}

	public ConnectionClosedException(String message, Throwable cause) {
		super(message, cause);
	}
}
