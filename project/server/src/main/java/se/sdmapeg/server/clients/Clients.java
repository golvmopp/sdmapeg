package se.sdmapeg.server.clients;


import se.sdmapeg.common.IdGenerator;
import se.sdmapeg.serverclient.ClientTaskId;
import se.sdmapeg.serverworker.TaskId;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Clients {
	private ConcurrentMap<TaskId, ClientTaskId> idMap;
	private ConcurrentMap<TaskId, Client> clientMap;
	private ConcurrentMap<InetAddress, Client> addressMap;
	private IdGenerator<TaskId> idGenerator;

	private Clients(IdGenerator<TaskId> idGenerator) {
		this.idGenerator = idGenerator;
		this.idMap = new ConcurrentHashMap<TaskId, ClientTaskId>();
		this.clientMap = new ConcurrentHashMap<TaskId, Client>();
		this.addressMap = new ConcurrentHashMap<InetAddress, Client>();
	}

	public void addClient(Client c) {
		addressMap.put(c.getAddress(), c);
	}

	public ClientTaskId getClientTaskId(TaskId id) {
		return idMap.get(id);
	}

	public Client getClient(TaskId id) {
		return clientMap.get(id);
	}

	public Set<Client> allClients() {
		return new HashSet<Client>(addressMap.values());
	}

	public Client getClientByAddress(InetAddress address) {
		return addressMap.get(address);
	}

	public void remove(Client client) {

	}

	public TaskId addTask(Client client, ClientTaskId clientTaskId) {
		TaskId taskId = idGenerator.newId();
		idMap.put(taskId, clientTaskId);
		clientMap.put(taskId, client);
		return taskId;
	}
}