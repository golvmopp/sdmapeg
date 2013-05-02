package se.sdmapeg.server.clients.callbacks;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import se.sdmapeg.serverworker.TaskId;

/**
 * A listener to be notified by a ClientManager when an event occurs.
 */
public interface ClientManagerListener {
	/**
	 * Notifies this listener that a new client with the specified address has
	 * connected.
	 *
	 * @param address the address of the connected client
	 */
	void clientConnected(InetSocketAddress address);

	/**
	 * Notifies this listener that the client with the specified address has
	 * disconnected.
	 *
	 * @param address the address of the disconnected client
	 */
	void clientDisconnected(InetSocketAddress address);

	
	/**
	 * Notifies this listener that a new task with the specified TaskId has been
	 * received from the client with the specified address.
	 *
	 * @param taskId the ID of the new task
	 * @param address the address of the client the task was received from
	 */
	void taskReceived(TaskId taskId, InetSocketAddress address);

	/**
	 * Notifies this listener that the task with the specified TaskId has been
	 * cancelled by the client with the specified address.
	 *
	 * @param taskId the ID of the task which was cancelled
	 * @param address the address of the client which cancelled the task
	 */
	void taskCancelled(TaskId taskId, InetSocketAddress address);

	/**
	 * Notifies this listener that the result for the task with the specified
	 * TaskId has been sent to the client with the specified address.
	 *
	 * @param taskId the ID of the completed task
	 * @param address the address of the client which the result was sent to
	 */
	void resultSent(TaskId taskId, InetSocketAddress address);
}
