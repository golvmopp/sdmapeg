package se.sdmapeg.server.workers;

import java.io.IOException;
import java.net.InetAddress;
import se.sdmapeg.common.communication.CommunicationException;
import se.sdmapeg.serverworker.ServerToWorkerMessage;
import se.sdmapeg.serverworker.WorkerToServerMessage;

/**
 * Represents the client. Handles communication between Server and Client.
 */
public interface Worker {
	/**
	 * returns the address of the worker.
	 * @return the address of the worker
	 */
	InetAddress getAddress();

	/**
	 * Sends a message from server to worker.
	 * @param message message to send
	 * @exception IOException if an error occurred
	 */
	void send(ServerToWorkerMessage message) throws CommunicationException;

	/**
	 * Receives a message from the worker. This method blocks until a message has been received
	 * @return received message
	 * @exception IOException if an error occurred
	 */
	WorkerToServerMessage receive() throws CommunicationException;

	/**
	 * Disconnects the worker.
	 */
	void disconnect();

	/**
	 * Returns the number of processor cores in the worker.
	 * @return number of processor cores in the worker
	 */
	int getParallellWorkCapacity();
}
