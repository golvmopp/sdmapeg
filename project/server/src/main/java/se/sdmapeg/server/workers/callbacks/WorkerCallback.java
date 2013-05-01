package se.sdmapeg.server.workers.callbacks;

import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.serverworker.TaskId;

/**
 * A callback to be notified by a Worker when an event occurs.
 */
public interface WorkerCallback {

	/**
	 * Notifies this callback that the task with the specified TaskId has been
	 * completed by the worker.
	 *
	 * @param taskId the id of the completed task
	 * @param result the result of the completed task
	 */
	void taskCompleted(TaskId taskId, Result<?> result);

	/**
	 * Notifies this callback that the task with the specified TaskId was
	 * successfully stolen from the worker.
	 *
	 * @param taskId the id of the stolen task
	 */
	void taskStolen(TaskId taskId);

	/**
	 * Notifies this callback that the worker has disconnected.
	 *
	 */
	void workerDisconnected();

	/**
	 * Notifies this callback that the worker has a shortage of work and is
	 * requesting more tasks to perform.
	 */
	void workRequested();
	
}
