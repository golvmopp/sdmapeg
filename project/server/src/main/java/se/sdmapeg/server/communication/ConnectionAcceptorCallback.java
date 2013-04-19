package se.sdmapeg.server.communication;

import se.sdmapeg.common.communication.Connection;
import se.sdmapeg.common.communication.Message;

/**
 * A callback to be notified by a connection acceptor when an event occurs.
 */
public interface ConnectionAcceptorCallback<S extends Message, R extends Message> {

	/**
	 * Notifies this callback that a new connection has been received.
	 *
	 * @param connection the received connection
	 */
	void connectionReceived(Connection<S, R> connection);

	/**
	 * Notifies this callback that the connection handler has been closed.
	 */
	void connectionHandlerClosed();
	
}
