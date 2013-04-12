package se.sdmapeg.server.clients;


import se.sdmapeg.common.IdGenerator;
import se.sdmapeg.serverclient.ClientTaskId;
import se.sdmapeg.serverworker.TaskId;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

final class Clients {
	private final Map<TaskId, ClientTaskId> idMap;
	private final Map<TaskId, Client> taskToClientMap;
	private final Map<Client, Set<TaskId>> clientToTaskMap;
	private final Map<InetAddress, Client> addressMap;
	private final Map<Client, Lock> lockMap;
	private final IdGenerator<TaskId> idGenerator;

	private Clients(IdGenerator<TaskId> idGenerator) {
		this.idGenerator = idGenerator;
		this.idMap = new ConcurrentHashMap<>();
		this.taskToClientMap = new ConcurrentHashMap<>();
		this.clientToTaskMap = new ConcurrentHashMap<>();
		this.addressMap = new ConcurrentHashMap<>();
		this.lockMap = new ConcurrentHashMap<>();
	}

	public void addClient(Client client) {
		Lock lock = new ReentrantLock();
		try {
			lock.lock();
			lockMap.put(client, lock);
			addressMap.put(client.getAddress(), client);
			clientToTaskMap.put(client, Collections.newSetFromMap(new ConcurrentHashMap<TaskId, Boolean>()));
		} finally {
			lock.unlock();
		}
	}

	public ClientTaskId getClientTaskId(TaskId id) {
		return idMap.get(id);
	}

	public Client getClient(TaskId taskId) {
		return taskToClientMap.get(taskId);
	}

	public Set<Client> allClients() {
		return new HashSet<>(addressMap.values());
	}

	public Client getClientByAddress(InetAddress address) {
		return addressMap.get(address);
	}

	public void remove(Client client) {
		if (!acquireLock(client)) {
			return;
		}
		try {
			addressMap.remove(client.getAddress());
			Set<TaskId> tasks = clientToTaskMap.remove(client);
			for (TaskId task : tasks) {
				idMap.remove(task);
				taskToClientMap.remove(task);
			}
		} finally {
			Lock lock = lockMap.remove(client);
			lock.unlock();
		}
	}

	public TaskId addTask(Client client, ClientTaskId clientTaskId) {
		if (!acquireLock(client)) {
			return null;
		}
		try {
			TaskId taskId = idGenerator.newId();
			idMap.put(taskId, clientTaskId);
			taskToClientMap.put(taskId, client);
			clientToTaskMap.get(client).add(taskId);
			return taskId;
		} finally {
			releaseLock(client);
		}
	}

	public void finishTask(TaskId taskId) {
		Client client = taskToClientMap.get(taskId);
		if (client == null || !acquireLock(client)) {
			return;
		}
		try {
			Set<TaskId> tasks = clientToTaskMap.get(client);
			tasks.remove(taskId);
			idMap.remove(taskId);
			taskToClientMap.remove(taskId);
		} finally {
			releaseLock(client);
		}
	}

	private boolean acquireLock(Client client) {
		Lock lock = lockMap.get(client);
		if (lock == null) {
			return false;
		}
		lock.lock();
		if (!lockMap.containsKey(client)) {
			lock.unlock();
			return false;
		}
		return true;
	}

	private void releaseLock(Client client) {
		lockMap.get(client).unlock();
	}
}