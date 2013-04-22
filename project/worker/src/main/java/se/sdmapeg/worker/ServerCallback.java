package se.sdmapeg.worker;

import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.serverworker.TaskId;

/**
 * A callback to be notified by a Server when an event occurs.
 */
public interface ServerCallback {

	/**
	 * Notifies this callback that a task has been received from the server.
	 *
	 * @param taskId the ID of the received task
	 * @param task the received task
	 */
	void taskReceived(TaskId taskId, Task<?> task);

	/**
	 * Notifies this callback that a request to cancel the task with the
	 * specified TaskId has been received from the server.
	 *
	 * @param taskId the ID of the task requested to be cancelled
	 */
	void taskCancelled(TaskId taskId);

	/**
	 * Notifies the callback that a request to steal the desired number of tasks
	 * has been received from the server.
	 *
	 * @param desired the number of tasks desired to be stolen
	 */
	void workStealingRequested(int desired);

	/**
	 * Notifies this callback that the connection to the server has been closed.
	 */
	void connectionClosed();
}
