package se.sdmapeg.server.clients.models;

import se.sdmapeg.server.clients.callbacks.ClientManagerListenerSupport;
import se.sdmapeg.server.clients.callbacks.ClientManagerListener;
import se.sdmapeg.server.clients.callbacks.ClientManagerCallback;
import se.sdmapeg.server.clients.exceptions.ClientRejectedException;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sdmapeg.common.listeners.Listenable;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.serverworker.TaskId;

/**
 * A model containing the internal state and logic of a ClientManager.
 */
public final class ClientManagerModel implements Listenable<ClientManagerListener> {
	private static final Logger LOG = LoggerFactory.getLogger(ClientManagerModel.class);
	private final ClientManagerListenerSupport listeners;
	private final ClientManagerCallback callback;
	private final ConcurrentMap<TaskId, Client> taskMap =
		new ConcurrentHashMap<>();
	private final ConcurrentMap<InetSocketAddress, Client> addressMap =
		new ConcurrentHashMap<>();

	/**
	 * Creates a new ClientManagerModel with the specified listener support and
	 * callback.
	 *
	 * @param listeners listener support to be used by this model
	 * @param callback callback to be notified of events
	 */
	public ClientManagerModel(ClientManagerListenerSupport listeners,
			ClientManagerCallback callback) {
		this.listeners = listeners;
		this.callback = callback;
	}

	/**
	 * Adds the specified client to this model. The client must have a unique
	 * address.
	 *
	 * @param client the client to be added
	 * @throws ClientRejectedException if a client with the same address was
	 *                                 already present
	 */
	public void addClient(Client client) throws ClientRejectedException {
		if (addressMap.putIfAbsent(client.getAddress(), client) == null) {
			LOG.info("{} connected", client);
			listeners.clientConnected(client.getAddress());
		} else {
			LOG.warn("Connection refused: {} attempted to connect, but was"
				+ " already connected", client);
			throw new ClientRejectedException();
		}
	}

	/**
	 * Returns the client with the specified address, or {@code null} if no
	 * client with said address was present.
	 *
	 * @param clientAddress the address of the client
	 * @return the client with the specified address, or {@code null} if no
	 *         client with said address was present
	 */
	public Client getClient(InetSocketAddress clientAddress) {
		return addressMap.get(clientAddress);
	}

	/**
	 * Returns an unmodifiable snapshot of all clients currently present.
	 *
	 * @return an unmodifiable snapshot of all clients currently present
	 */
	public Set<Client> getClients() {
		return Collections.unmodifiableSet(new HashSet<>(addressMap.values()));
	}

	/**
	 * Removes the specified client from this model.
	 *
	 * @param client the client to remove
	 */
	public void removeClient(Client client) {
		if (addressMap.remove(client.getAddress()) == null) {
			return;
		} 
		LOG.info("{} disconnected", client);
		listeners.clientDisconnected(client.getAddress());
		for (TaskId task : client.getActiveTasks()) {
			LOG.info("Cancelling task {}", task);
			cancelTask(task);
		}
	}

	/**
	 * Handles the result of the task with the specified task ID by notifying
	 * the appropriate client.
	 *
	 * @param taskId the id of the completed task
	 * @param result the result of the completed task
	 */
	public void handleResult(TaskId taskId, Result<?> result) {
		Client client = taskMap.remove(taskId);
		if (client == null) {
			return;
		}
		client.taskCompleted(taskId, result);
		LOG.info("Result for Task {} sent to {}", taskId, client);
		listeners.resultSent(taskId, client.getAddress());
	}

	/**
	 * Adds the specified task with the specified ID sent from the specified
	 * client.
	 *
	 * @param client the client which requested the task to be performed
	 * @param taskId the ID of the task
	 * @param task the task itself
	 */
	public void addTask(Client client, TaskId taskId, Task<?> task) {
		if (taskMap.putIfAbsent(taskId, client) != null) {
			return;
		}
		if (!addressMap.containsKey(client.getAddress())) {
			taskMap.remove(taskId);
			return;
		}
		LOG.info("Task {} received from {}", taskId, client);
		listeners.taskReceived(taskId, client.getAddress());
		callback.handleTask(taskId, task);
	}

	/**
	 * Cancels the task with the specified ID.
	 *
	 * @param task the ID of the task to cancel
	 */
	public void cancelTask(TaskId task) {
		Client client = taskMap.remove(task);
		if (client == null) {
			return;
		}
		LOG.info("Task {} cancelled by {}", task, client);
		listeners.taskCancelled(task, client.getAddress());
		callback.cancelTask(task);
	}

	@Override
	public void addListener(ClientManagerListener listener) {
		listeners.addListener(listener);
	}

	@Override
	public void removeListener(ClientManagerListener listener) {
		listeners.removeListener(listener);
	}
}
