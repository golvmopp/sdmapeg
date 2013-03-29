package se.sdmapeg.client;

import se.sdmapeg.serverclient.ClientToServerMessage;
import se.sdmapeg.serverclient.ServerToClientMessage;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Represents the server. Handles communication between Client and Server.
 */
public interface Server {
	/**
	 * returns the address of the server.
	 * @return the address of the server
	 */
	InetAddress getAddress();

	/**
	 * Sends a message from the client to the server.
	 * @param message message to send
	 */
	void send(ClientToServerMessage message) throws IOException;

	/**
	 * Receives a message from the server. This method blocks until a message has been received.
	 * @return received message.
	 */
	ServerToClientMessage receive() throws InterruptedException, IOException;

	/**
	 * Disconnects from the server. If no connection is open, no action is performed.
	 */
	void disconnect();
}
