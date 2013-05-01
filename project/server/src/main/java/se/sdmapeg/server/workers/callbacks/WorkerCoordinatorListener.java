package se.sdmapeg.server.workers.callbacks;

import java.net.InetAddress;
import se.sdmapeg.serverworker.TaskId;

/**
 * A listener to be notified by a WorkerCoordinator when an event occurs.
 */
public interface WorkerCoordinatorListener {
	/**
	 * Notifies this listener that a new worker with the specified address has
	 * connected.
	 *
	 * @param address the address of the connected worker
	 */
	void workerConnected(InetAddress address);

	/**
	 * Notifies this listener that the worker with the specified address has
	 * disconnected.
	 *
	 * @param address the address of the disconnected worker
	 */
	void workerDisconnected(InetAddress address);
	
	/**
	 * Notifies this listener that the result for the task with the specified
	 * TaskId has been received from the worker with the specified address.
	 *
	 * @param taskId the ID of the completed task
	 * @param address the address of the worker which completed the task
	 */
	void resultReceived(TaskId taskId, InetAddress address);

	/**
	 * Notifies this listener that the task with the specified TaskId has been
	 * assigned to the worker with the specified address.
	 *
	 * @param taskId the ID of the assigned task
	 * @param address the address of the worker which the task was assigned to
	 */
	void taskAssigned(TaskId taskId, InetAddress address);

	/**
	 * Notifies this listener that the task with the specified TaskId was
	 * aborted by the worker with the specified address.
	 *
	 * @param taskId the ID of the aborted task
	 * @param address the address of the worker which aborted the task
	 */
	void taskAborted(TaskId taskId, InetAddress address);
}
