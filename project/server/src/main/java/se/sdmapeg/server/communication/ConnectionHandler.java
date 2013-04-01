package se.sdmapeg.server.communication;

import se.sdmapeg.common.communication.Connection;
import se.sdmapeg.common.communication.Message;

import java.io.Closeable;
import java.net.SocketException;

import se.sdmapeg.common.communication.CommunicationException;

/**
 * Interface for classes handling connections.
 *
 * @param <S> Message type that can be sent over connections accepted by this connection handler
 * @param <R> Message type that can be received over connections accepted by this connection handler
 */
public interface ConnectionHandler<S extends Message, R extends Message> extends Closeable {
	/**
	 * Accepts a new connection. This method blocks until a connection has been received.
	 *
	 * @return The accepted connection
	 * @throws CommunicationException If an error occurred
	 * @throws SocketException        If the socket was closed while waiting for a connection
	 */
	Connection<S, R> accept() throws CommunicationException, SocketException;

	/**
	 * Returns whether or not the connection is open.
	 *
	 * @return whether or not the connection is open
	 */
	boolean isOpen();
}
