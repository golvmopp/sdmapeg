package se.sdmapeg.server.workers;

import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.serverworker.TaskId;

import java.net.InetAddress;

/**
 * Interface for classes coordinating workers.
 */
public interface WorkerCoordinator {

	/**
	 * Handles the task.
	 *
	 * @param taskId Unique id paired to a task
	 * @param task   Task to be handled
	 */
	void handleTask(TaskId taskId, Task<?> task);

	/**
	 * Cancels the task with the specified TaskId.
	 *
	 * @param taskId the id of the task to cancel
	 */
	void cancelTask(TaskId taskId);

	/**
	 * Shuts down this WorkerCoordinator. By disconnecting all workers and stops listening to new connections.
	 */
	void shutDown();

	/**
	 * Disconnects a specific worker.
	 *
	 * @param workerAddress Address to the worker.
	 */
	void disconnectWorker(InetAddress workerAddress);

	/**
	 * Starts the Worker Coordinator.
	 */
	void start();

	/**
	 * Returns the current state of this worker coordinator.
	 */
	State getState();

	/**
	 * Enum representing the state of a worker coordinator.
	 */
	public enum State {
		/**
		 * Indicates that a worker coordinator has been created but not yet
		 * started.
		 */
		CREATED,
		/**
		 * Indicates that a worker coordinator has been created and started.
		 */
		STARTED,
		/**
		 * Indicates that a worker coordinator has been stopped.
		 */
		STOPPED;
	}
}
