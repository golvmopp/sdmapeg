package se.sdmapeg.server.clients;

import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.serverworker.TaskId;

import java.net.InetAddress;

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
	State getState();

	/**
	 * Enum representing the state of a client manager.
	 */
	public enum State {
		/**
		 * Indicates that a client manager has been created but not yet started.
		 */
		CREATED,
		/**
		 * Indicates that a client manager has been created and started.
		 */
		STARTED,
		/**
		 * Indicates that a client manager has been stopped.
		 */
		STOPPED;
	}
}
