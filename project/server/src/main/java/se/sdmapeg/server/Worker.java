package se.sdmapeg.server;

import se.sdmapeg.serverworker.ServerToWorkerMessage;
import se.sdmapeg.serverworker.WorkerToServerMessage;

import java.io.IOException;
import java.net.InetAddress;

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
	 */
	void send(ServerToWorkerMessage message) throws IOException;

	/**
	 * Receives a message from the worker. This method blocks until a message has been received.
	 * @return received message.
	 */
	WorkerToServerMessage receive();

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
