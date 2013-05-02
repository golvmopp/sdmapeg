package se.sdmapeg.client;

import java.net.InetSocketAddress;

import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.serverclient.ClientTaskId;

/**
 * Represents the server. Handles communication between Client and Server.
 */
public interface Server {
	/**
	 * returns the address of the server.
	 *
	 * @return the address of the server
	 */
	InetSocketAddress getAddress();

	/**
	 * Requests the server to perform the specified task.
	 *
	 * @param taskId the client-side ID of the task requested to be performed
	 * @param task the task requested to be performed
	 */
	void performTask(ClientTaskId taskId, Task<?> task);

	/**
	 * Requests the server to cancel the task with the specified ID.
	 *
	 * @param taskId the client-side ID of the task to be cancelled
	 */
	void cancelTask(ClientTaskId taskId);

	/**
	 * Continually listens to input from this Server, and calls the appropriate
	 * methods of the specified  callback when an input has been received.
	 * This method will keep running until the connection to this Server is
	 * closed, and will always end with calling the connectionClosed method of
	 * the callback.
	 */
	void listen(ServerCallback callback);
	
	/**
	 * Disconnects from the server. If no connection is open, no action is
	 * performed.
	 */
	void disconnect();
}
