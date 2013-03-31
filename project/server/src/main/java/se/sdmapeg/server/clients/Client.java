package se.sdmapeg.server.clients;

import java.io.IOException;
import java.net.InetAddress;
import se.sdmapeg.common.communication.CommunicationException;

import se.sdmapeg.serverclient.communication.ClientToServerMessage;
import se.sdmapeg.serverclient.communication.ServerToClientMessage;

/**
 * Represents the client. Handles communication between Server and Client.
 */
public interface Client {
	/**
	 * returns the address of the client.
	 * @return the address of the client
	 */
	InetAddress getAddress();

	/**
	 * Sends a message from server to client.
	 * @param message message to send
	 * @exception CommunicationException if an error occurred
	 */
	void send(ServerToClientMessage message) throws CommunicationException;

	/**
	 * Receives a message from the client. This method blocks until a message has been received.
	 * @return received message.
	 * @exception CommunicationException if an error occurred
	 */
	ClientToServerMessage receive() throws CommunicationException;

	/**
	 * Disconnects the client.
	 */
	void disconnect();
}
