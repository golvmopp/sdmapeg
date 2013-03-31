package se.sdmapeg.server;

import se.sdmapeg.common.Result;
import se.sdmapeg.serverworker.TaskId;

import java.net.InetAddress;

/**
 * Interface for classes managing clients.
 */
public interface ClientManager {

	/**
	 * Handles the result of the task represented by the TaskId.
	 * @param taskId Unique id paired to a task
	 * @param result Result to task represented by the TaskId
	 */
	void handleResult(TaskId taskId, Result<?> result);

	/**
	 * Shuts down this ClientManager. By disconnecting all clients and stops listening to new connections.
	 */
	void shutDown();

	/**
	 * Disconnects a specific client.
	 * @param clientAddress Address to the client.
	 */
	void disconnectClient(InetAddress clientAddress);
	
	/**
	 * Starts the Client Manager.
	 */
	void start();
	
	/**
	 * Checks whether the Client Manager is running.
	 */
	boolean isRunning();
	
	/**
	 * Returns true if the Client Manager has been started at least once.
	 */
	boolean isStarted();
}
