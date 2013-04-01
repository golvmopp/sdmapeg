package se.sdmapeg.server.workers;

import java.io.IOException;
import java.net.InetAddress;

import se.sdmapeg.common.communication.CommunicationException;
import se.sdmapeg.common.communication.ConnectionClosedException;
import se.sdmapeg.serverworker.ServerToWorkerMessage;
import se.sdmapeg.serverworker.WorkerToServerMessage;

/**
 * Represents the client. Handles communication between Server and Client.
 */
public interface Worker {
	/**
	 * returns the address of the worker.
	 *
	 * @return the address of the worker
	 */
	InetAddress getAddress();

	/**
	 * Sends a message from server to worker.
	 *
	 * @param message message to send
	 * @throws CommunicationException if an error occurred
	 * @throws ConnectionClosedException if the connection was closed
	 */
	void send(ServerToWorkerMessage message) throws CommunicationException,
													ConnectionClosedException;

	/**
	 * Receives a message from the worker. This method blocks until a message has been received
	 *
	 * @return received message
	 * @throws CommunicationException if an error occurred
	 * @throws ConnectionClosedException if the connection was closed
	 */
	WorkerToServerMessage receive() throws CommunicationException,
										   ConnectionClosedException;

	/**
	 * Disconnects the worker.
	 */
	void disconnect();

	/**
	 * Returns the number of processor cores in the worker.
	 *
	 * @return number of processor cores in the worker
	 */
	int getParallellWorkCapacity();
}
