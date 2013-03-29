package se.sdmapeg.server;

import se.sdmapeg.common.Task;
import se.sdmapeg.serverworker.TaskId;

import java.net.InetAddress;

/**
 * Interface for classes coordinating workers.
 */
public interface WorkerCoordinator {

	/**
	 * Handles the task.
	 * @param taskId Unique id paired to a task
	 * @param task Task to be handled
	 */
	void handleTask(TaskId taskId, Task<?> task);

	/**
	 * Shuts down this WorkerCoordinator. By disconnecting all workers and stops listening to new connections.
	 */
	void shutDown();

	/**
	 * Disconnects a specific worker.
	 * @param workerAddress Address to the worker.
	 */
	void disconnectWorker(InetAddress workerAddress);
}
