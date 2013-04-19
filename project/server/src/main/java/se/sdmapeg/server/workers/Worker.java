package se.sdmapeg.server.workers;

import java.net.InetAddress;
import java.util.Set;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.serverworker.TaskId;

/**
 * Interface for representing a worker connected to the server.
 */
interface Worker {
	/**
	 * Returns the address of this worker.
	 *
	 * @return the address of this worker
	 */
	InetAddress getAddress();

	/**
	 * Attempts to assign a task to this Worker, and returns true if the
	 * assignment was successful. The worker will attempt to perform the
	 * task and then return its result through the taskCompleted method of the
	 * Callback. If the task could not be assigned for some reason (e.g. due to
	 * the worker being shut down), this method will return false, and no side
	 * effects of assigning the task will be seen. If the task is successfully
	 * assigned it will be present in the Set returned by getActiveTasks until
	 * it has been successfully completed.
	 *
	 * @param taskId the TaskId of the task
	 * @param task the Task to assign this worker
	 */
	boolean assignTask(TaskId taskId, Task<?> task);

	/**
	 * Cancels the execution of the task with the specified TaskId. If the task
	 * has already begun executing, an attempt to abort it will be made.
	 *
	 * @param taskId the TaskId of the task to cancel
	 */
	void cancelTask(TaskId taskId);

	/**
	 * Returns an immutable snapshot of the IDs of all currently active tasks.
	 * An active task is a task that has been assigned to this worker but not
	 * yet completed or cancelled.
	 *
	 * @return a snapshot of all currently active tasks
	 */
	Set<TaskId> getActiveTasks();

	/**
	 * Returns the current load on this worker. The load is the number of tasks
	 * assigned to this worker minus this worker's known parallel work capacity.
	 * In other words; the load is the amount of excess work that this worker
	 * has queued up. Note that the load may also be negative, if the worker
	 * does not have enough tasks assigned to work at full capacity.
	 *
	 * @return the current workload on this worker
	 */
	int getLoad();

	/**
	 * Attempts to steal a number of tasks from this worker's work queue. This
	 * method will cause the worker to cancel at most a number equal to the
	 * specified maximum number of tasks which are currently queued to be
	 * performed by the worker. The taskStolen method of the Callback will
	 * be invoked for each cancelled task. Note that the max parameter
	 * specified the <b>maximum</b> number of tasks to cancel; it is perfectly
	 * acceptable for a worker to cancel fewer or none at all.
	 *
	 * @param max the maximum number of tasks to steal
	 */
	void stealTasks(int max);

	/**
	 * Continually listens to input from this Worker, and calls the appropriate
	 * methods of the callback when an input has been received. The methods of
	 * the callback will only be called while a thread is running this method,
	 * and will only ever be called by that thread, if the callback is not
	 * shared with other objects. This method will keep running until this
	 * Worker is disconnected, and will always end with calling the
	 * workerDisconnected method of the callback with this Worker as the
	 * argument.
	 */
	void listen();

	/**
	 * Disconnects the worker.
	 */
	void disconnect();

	/**
	 * Returns the number of processor cores in the worker.
	 *
	 * @return number of processor cores in the worker
	 */
	int getParallellWorkCapacity();

	/**
	 * Returns true if this Worker is currently accepting work. While this
	 * method returns false, all calls to assignTask will also return false.
	 *
	 * @return whether this Worker is currently accepting new tasks 
	 */
	boolean isAcceptingWork();
}
