package se.sdmapeg.server.workers;

import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.serverworker.TaskId;

/**
 * A callback for providing asynchronous responses to commands sent to a
 * Worker.
 */
interface WorkerCallback {

	/**
	 * Called to indicate that the specified Worker has completed the task
	 * with the specified TaskId.
	 *
	 * @param worker the Worker which completed the task
	 * @param taskId the id of the completed task
	 * @param result the result of the completed task
	 */
	void taskCompleted(Worker worker, TaskId taskId,
					   Result<?> result);

	/**
	 * Called to indicate that the task with the specified TaskId was
	 * successfully stolen from the specified Worker.
	 *
	 * @param worker the Worker from which the task was stolen
	 * @param taskId the id of the stolen task
	 */
	void taskStolen(Worker worker, TaskId taskId);

	/**
	 * Called to indicate that the specified Worker has disconnected.
	 *
	 * @param worker the Worker which was disconnected
	 */
	void workerDisconnected(Worker worker);

	/**
	 * Called to indicate that the specified worker has a shortage of work
	 * and is requesting more tasks to perform.
	 */
	void workRequested(Worker worker);
	
}
