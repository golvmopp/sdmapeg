package se.sdmapeg.worker;

import java.net.InetAddress;

import se.sdmapeg.common.communication.CommunicationException;
import se.sdmapeg.common.communication.ConnectionClosedException;
import se.sdmapeg.serverworker.ServerToWorkerMessage;
import se.sdmapeg.serverworker.WorkerToServerMessage;

/**
 * Represents the server. Handles communication between Worker and Server.
 */
public interface Server {

	/**
	 * returns the address of the server.
	 *
	 * @return the address of the server
	 */
	InetAddress getAddress();

	/**
	 * Sends a message from the worker to the server.
	 *
	 * @param message message to send
	 * @throws CommunicationException if an error occurred
	 * @throws ConnectionClosedException if the connection was closed
	 */
	void send(WorkerToServerMessage message) throws CommunicationException,
													ConnectionClosedException;

	/**
	 * Receives a message from the server. This method blocks until a message
	 * has been received.
	 *
	 * @return received message.
	 * @throws CommunicationException if an error occurred
	 * @throws ConnectionClosedException if the connection was closed
	 */
	ServerToWorkerMessage receive() throws CommunicationException,
										   ConnectionClosedException;

	/**
	 * Disconnects from the server. If no connection is open, no action is
	 * performed.
	 */
	void disconnect();
}
