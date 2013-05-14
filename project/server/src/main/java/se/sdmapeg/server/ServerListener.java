package se.sdmapeg.server;

import java.net.InetSocketAddress;
import se.sdmapeg.serverworker.TaskId;

/**
 * A listener to be notified by a Server when an event occurs.
 */
public interface ServerListener {
	/**
	 * Notifies this listener that a new client with the specified address has
	 * connected.
	 *
	 * @param clientAddress the address of the connected client
	 */
	void clientConnected(InetSocketAddress clientAddress);

	/**
	 * Notifies this listener that the client with the specified address has
	 * disconnected.
	 *
	 * @param clientAddress the address of the disconnected client
	 */
	void clientDisconnected(InetSocketAddress clientAddress);

	/**
	 * Notifies this listener that a new worker with the specified address has
	 * connected.
	 *
	 * @param workerAddress the address of the connected worker
	 */
	void workerConnected(InetSocketAddress workerAddress);

	/**
	 * Notifies this listener that the worker with the specified address has
	 * disconnected.
	 *
	 * @param workerAddress the address of the disconnected worker
	 */
	void workerDisconnected(InetSocketAddress workerAddress);

	/**
	 * Notifies this listener that a new task with the specified TaskId has been
	 * received from the client with the specified address.
	 *
	 * @param taskId the ID of the new task
	 * @param clientAddress the address of the client the task was received from
	 */
	void taskReceivedFromClient(TaskId taskId, InetSocketAddress clientAddress);

	/**
	 * Notifies this listener that the task with the specified TaskId has been
	 * cancelled by the client with the specified address.
	 *
	 * @param taskId the ID of the task which was cancelled
	 * @param clientAddress the address of the client which cancelled the task
	 */
	void taskCancelledByClient(TaskId taskId, InetSocketAddress clientAddress);

	/**
	 * Notifies this listener that the result for the task with the specified
	 * TaskId has been sent to the client with the specified address.
	 *
	 * @param taskId the ID of the completed task
	 * @param clientAddress	the address of the client which the result was sent
	 *						to
	 */
	void resultSentToClient(TaskId taskId, InetSocketAddress clientAddress);

	/**
	 * Notifies this listener that the result for the task with the specified
	 * TaskId has been received from the worker with the specified address.
	 *
	 * @param taskId the ID of the completed task
	 * @param workerAddress the address of the worker which completed the task
	 */
	void resultReceivedFromWorker(TaskId taskId, InetSocketAddress workerAddress);

	/**
	 * Notifies this listener that the task with the specified TaskId has been
	 * assigned to the worker with the specified address.
	 *
	 * @param taskId the ID of the assigned task
	 * @param workerAddress	the address of the worker which the task was
	 *						assigned to
	 */
	void taskAssignedToWorker(TaskId taskId, InetSocketAddress workerAddress);

	/**
	 * Notifies this listener that the task with the specified TaskId was
	 * aborted by the worker with the specified address.
	 *
	 * @param taskId the ID of the aborted task
	 * @param workerAddress the address of the worker which aborted the task
	 */
	void taskAbortedByWorker(TaskId taskId, InetSocketAddress workerAddress);
}
