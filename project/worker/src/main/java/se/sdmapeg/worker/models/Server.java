package se.sdmapeg.worker.models;

import java.net.InetSocketAddress;
import java.util.Set;

import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.serverworker.TaskId;

/**
 * Represents the server. Handles communication between Worker and Server.
 */
public interface Server {

	/**
	 * returns the address of the server.
	 *
	 * @return the address of the server
	 */
	InetSocketAddress getAddress();

	/**
	 * Notifies the server that the task with the specified TaskId has been
	 * completed with the specified Result.
	 *
	 * @param taskId the ID of the task that was completed
	 * @param result the result of the completed task
	 */
	void taskCompleted(TaskId taskId, Result<?> result);

	/**
	 * Notifies the server that the set of tasks with the specified TaskIds have
	 * successfully been stolen from the work queue.
	 *
	 * @param tasks the TaskIds of the stolen tasks
	 */
	void tasksStolen(Set<TaskId> tasks);

	/**
	 * Sends an identification message with the specified data to the server.
	 *
	 * @param parallelWorkCapacity	the number of tasks that the worker
	 *								identifying itself is capable of
	 *								performing in parallel
	 */
	void identify(int parallelWorkCapacity);

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
