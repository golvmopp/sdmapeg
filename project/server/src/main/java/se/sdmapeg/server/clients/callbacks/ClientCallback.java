package se.sdmapeg.server.clients.callbacks;

import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.serverworker.TaskId;

/**
 * A callback to be notified by a Client when an event occurs.
 */
public interface ClientCallback {

	/**
	 * Notifies this callback that a task has been received from the client.
	 *
	 * @param taskId the ID of the received task
	 * @param task the received task
	 */
	void taskReceived(TaskId taskId, Task<?> task);

	/**
	 * Notifies this callback that a request to cancel the task with the
	 * specified TaskId has been received from the client.
	 *
	 * @param taskId the ID of the task requested to be cancelled
	 */
	void taskCancelled(TaskId taskId);

	/**
	 * Notifies this callback that the client has disconnected.
	 */
	void clientDisconnected();
	
}
