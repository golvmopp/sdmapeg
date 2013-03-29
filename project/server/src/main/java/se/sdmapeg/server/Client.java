package se.sdmapeg.server;

import se.sdmapeg.serverclient.ClientToServerMessage;
import se.sdmapeg.serverclient.ServerToClientMessage;

import java.io.IOException;
import java.net.InetAddress;

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
	 */
	void send(ServerToClientMessage message) throws IOException;

	/**
	 * Receives a message from the client. This method blocks until a message has been received.
	 * @return received message.
	 */
	ClientToServerMessage receive() throws InterruptedException, IOException;

	/**
	 * Disconnects the client.
	 */
	void disconnect();
}
