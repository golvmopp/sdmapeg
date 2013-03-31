package se.sdmapeg.common.communication;

import java.io.Closeable;
import java.net.InetAddress;

/**
 * Interface representing a network connection. The connection supports full
 * duplex communication, allowing sending and receiving messages at the same
 * time.
 *
 * @param <S> the type of messages which can be sent.
 * @param <R> the type of messages which can be received.
 */
public interface Connection<S extends Message, R extends Message> extends Closeable {
	/**
	 * Returns the address of the machine on the other end of this connection.
	 * @return the remote address
	 */
	InetAddress getAddress();

	/**
	 * Sends a message over this connection.
	 * @param message the message to be sent.
	 * @throws CommunicationException if an error occurs.
	 */
	void send(S message) throws CommunicationException;

	/**
	 * Receives a message from the other end of this connection. This method
	 * blocks until a message has been received. 
	 * @return the received message.
	 * @throws CommunicationException if an error occurs.
	 */
	R receive() throws CommunicationException;

	/**
	 * Returns whether this connection is open. 
	 * @return whether this connection is open.
	 */
	boolean isOpen();
}
