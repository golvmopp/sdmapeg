package se.sdmapeg.server.clients;

import java.net.InetAddress;
import java.util.Set;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.serverworker.TaskId;

/**
 * Interface for representing a client connected to the server. A client will
 * send tasks to the server, and wait for the tasks to be performed and have
 * their results sent back.
 */
interface Client {

	/**
	 * Disconnects this client. A disconnected client will no longer be able to
	 * communicate with the server.
	 */
	void disconnect();

	/**
	 * Returns an immutable snapshot of the IDs of all currently active tasks.
	 * An active task is a task that this client has requested to be performed,
	 * but has not yet been completed.
	 *
	 * @return a snapshot of all currently active tasks
	 */
	Set<TaskId> getActiveTasks();

	/**
	 * Returns the address of this client.
	 *
	 * @return the address of this client
	 */
	InetAddress getAddress();

	/**
	 * Continually listens to input from this Client, and calls the appropriate
	 * methods of the callback when an input has been received. The methods of
	 * the callback will only be called while a thread is running this method,
	 * and will only ever be called by that thread, if the callback is not
	 * shared with other objects. This method will keep running until this
	 * Client is disconnected, and will always end with calling the
	 * clientDisconnected method of the callback with this Client as the
	 * argument.
	 */
	void listen();

	/**
	 * Notifies the client that the task with the specified TaskId has been
	 * completed with the specified Result. The task will be removed from the
	 * set of active tasks.
	 *
	 * @param taskId the ID of the task that was completed
	 * @param result the result of the completed task
	 */
	void taskCompleted(TaskId taskId, Result<?> result);
}
