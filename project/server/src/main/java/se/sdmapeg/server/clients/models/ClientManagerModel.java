package se.sdmapeg.server.clients.models;

import se.sdmapeg.server.clients.callbacks.ClientManagerListenerSupport;
import se.sdmapeg.server.clients.callbacks.ClientManagerListener;
import se.sdmapeg.server.clients.callbacks.ClientManagerCallback;
import se.sdmapeg.server.clients.exceptions.ClientRejectedException;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
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
 *
 * @author niclas
 */
public final class ClientManagerModel implements Listenable<ClientManagerListener> {
	private static final Logger LOG =
								LoggerFactory.getLogger(ClientManagerModel.class);
	private final ClientManagerListenerSupport listeners;
	private final ClientManagerCallback callback;
	private final ConcurrentMap<TaskId, Client> taskMap =
									  new ConcurrentHashMap<>();
	private final ConcurrentMap<InetSocketAddress, Client> addressMap =
													 new ConcurrentHashMap<>();

	public ClientManagerModel(ClientManagerListenerSupport listeners,
							  ClientManagerCallback callback) {
		this.listeners = listeners;
		this.callback = callback;
	}

	public void addClient(Client client) throws ClientRejectedException {
		if (addressMap.putIfAbsent(client.getAddress(), client) == null) {
			LOG.info("{} connected", client);
			listeners.clientConnected(client.getAddress());
		}
		else {
			LOG.warn("Connection refused: {} attempted to connect, but was" +
					 " already connected", client);
			throw new ClientRejectedException();
		}
	}

	public Client getClient(InetSocketAddress clientAddress) {
		return addressMap.get(clientAddress);
	}

	public Set<Client> getClients() {
		return Collections.unmodifiableSet(new HashSet<>(addressMap.values()));
	}

	public void removeClient(Client client) {
		addressMap.remove(client.getAddress());
		LOG.info("{} disconnected", client);
		listeners.clientDisconnected(client.getAddress());
		for (TaskId task : client.getActiveTasks()) {
			LOG.info("Cancelling task {}", task);
			cancelTask(task);
		}
	}

	public void handleResult(TaskId taskId,
							 Result<?> result) {
		Client client = taskMap.remove(taskId);
		if (client == null) {
			return;
		}
		client.taskCompleted(taskId, result);
		LOG.info("Result for Task {} sent to {}", taskId, client);
		listeners.resultSent(taskId, client.getAddress());
	}

	public void addTask(Client client, TaskId taskId,
						Task<?> task) {
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
