package se.sdmapeg.server.clients;

import java.net.InetAddress;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.serverworker.TaskId;

/**
 * Interface for classes managing clients.
 */
public interface ClientManager {

	/**
	 * Handles the result of the task represented by the TaskId.
	 *
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
	 *
	 * @param clientAddress Address to the client.
	 */
	void disconnectClient(InetAddress clientAddress);

	/**
	 * Starts the Client Manager.
	 */
	void start();

	/**
	 * Returns the current state of this client manager.
	 */
	ClientManagerState getState();
}
