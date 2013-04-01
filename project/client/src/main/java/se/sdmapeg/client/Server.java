package se.sdmapeg.client;

import java.net.InetAddress;
import java.nio.channels.ConnectionPendingException;

import se.sdmapeg.common.communication.CommunicationException;
import se.sdmapeg.common.communication.ConnectionClosedException;

import se.sdmapeg.serverclient.communication.ClientToServerMessage;
import se.sdmapeg.serverclient.communication.ServerToClientMessage;

/**
 * Represents the server. Handles communication between Client and Server.
 */
public interface Server {
	/**
	 * returns the address of the server.
	 *
	 * @return the address of the server
	 */
	InetAddress getAddress();

	/**
	 * Sends a message from the client to the server.
	 *
	 * @param message message to send
	 * @throws CommunicationException if an error occurred
	 * @throws ConnectionClosedException if the connection was closed
	 */
	void send(ClientToServerMessage message) throws CommunicationException,
													ConnectionClosedException;

	/**
	 * Receives a message from the server. This method blocks until a message
	 * has been received.
	 *
	 * @return received message.
	 * @throws CommunicationException if an error occurred
	 * @throws ConnectionClosedException if the connection was closed
	 */
	ServerToClientMessage receive() throws CommunicationException,
										   ConnectionClosedException;

	/**
	 * Disconnects from the server. If no connection is open, no action is
	 * performed.
	 */
	void disconnect();
}
