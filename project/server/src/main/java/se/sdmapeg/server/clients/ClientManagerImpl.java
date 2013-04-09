package se.sdmapeg.server.clients;

import se.sdmapeg.common.IdGenerator;
import se.sdmapeg.common.communication.CommunicationException;
import se.sdmapeg.common.communication.Connection;
import se.sdmapeg.common.communication.ConnectionClosedException;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.server.communication.ConnectionHandler;
import se.sdmapeg.serverclient.ClientTaskId;
import se.sdmapeg.serverclient.communication.*;
import se.sdmapeg.serverworker.TaskId;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Implementation of ClientManager interface.
 */
public final class ClientManagerImpl implements ClientManager {
	private final AtomicReference<ClientManager> state;
	private final ClientManagerCallback clientManagerCallback;
	private final ExecutorService connectionThreadPool;
	private final IdGenerator<TaskId> taskIdGenerator;

	public ClientManagerImpl(ClientManagerCallback clientManagerCallback,
	                         ExecutorService connectionThreadPool,
	                         IdGenerator<TaskId> taskIdGenerator,
	                         ConnectionHandler<ServerToClientMessage, ClientToServerMessage> connectionHandler) {
		this.state = new AtomicReference<ClientManager>();
		this.state.set(new CreatedClientManager(connectionHandler));
		this.clientManagerCallback = clientManagerCallback;
		this.connectionThreadPool = connectionThreadPool;
		this.taskIdGenerator = taskIdGenerator;
	}

	@Override
	public void handleResult(TaskId taskId, Result<?> result) {
		state.get().handleResult(taskId, result);
	}

	@Override
	public void shutDown() {
		state.get().shutDown();
	}

	@Override
	public void disconnectClient(InetAddress clientAddress) {
		state.get().disconnectClient(clientAddress);
	}

	@Override
	public void start() {
		state.get().start();
	}

	@Override
	public State getState() {
		return state.get().getState();
	}

	private class CreatedClientManager implements ClientManager {
		private final ConnectionHandler<ServerToClientMessage, ClientToServerMessage> connectionHandler;

		private CreatedClientManager(
				ConnectionHandler<ServerToClientMessage, ClientToServerMessage> connectionHandler) {
			this.connectionHandler = connectionHandler;
		}

		@Override
		public void handleResult(TaskId taskId, Result<?> result) {
			throw new IllegalStateException();
		}

		@Override
		public void shutDown() {
			throw new IllegalStateException();
		}

		@Override
		public void disconnectClient(InetAddress clientAddress) {
			throw new IllegalStateException();
		}

		@Override
		public void start() {
			StartedClientManager newState = new StartedClientManager(connectionHandler);
			ClientManager oldState = ClientManagerImpl.this.state.get();
			boolean success = state.compareAndSet(oldState, newState);
			if (success) {
				connectionThreadPool.submit(new ConnectionAcceptor(newState, connectionHandler));
			} else {
				throw new IllegalStateException();
			}
		}

		@Override
		public State getState() {
			return ClientManager.State.CREATED;
		}
	}

	private class StartedClientManager implements ClientManager {
		private final Clients clients;
		private final ConnectionHandler<ServerToClientMessage, ClientToServerMessage> connectionHandler;
		private final Map<Client, ClientToServerMessage.Visitor<Void>> visitors = new ConcurrentHashMap<>();
		private volatile boolean shuttingDown = false;

		private StartedClientManager(ConnectionHandler<ServerToClientMessage,
				                             ClientToServerMessage> connectionHandler) {
			this.clients = new Clients();
			this.connectionHandler = connectionHandler;
		}

		public void clientConnected(Connection<ServerToClientMessage, ClientToServerMessage> connection) {
			Client client = ClientImpl.newClient(connection);
			synchronized (clients) {
				if (!shuttingDown) {
					clients.addClient(client);
					visitors.put(client, new ClientToServerMessageVisitor(client));
				} else {
					client.disconnect();
				}
			}
		}

		@Override
		public void handleResult(TaskId taskId, Result<?> result) {
			ServerToClientMessage resultMessage = ResultMessage.newResultMessage(clients.getClientTaskId(taskId), result);
			try {
				clients.getClient(taskId).send(resultMessage);
			} catch (CommunicationException e) {
				//TODO: Log this
				throw new AssertionError(e);
			}
		}

		@Override
		public void shutDown() {
			StoppedClientManager newState = new StoppedClientManager();
			ClientManager oldState = state.get();
			boolean success = state.compareAndSet(oldState, newState);
			if (success) {
				internalShutdown();
			} else {
				throw new IllegalStateException();
			}
		}

		private void internalShutdown() {
			shuttingDown = true;
			try {
				connectionHandler.close();
			} catch (IOException e) {
				// TODO: Log this
				throw new AssertionError(e);
			}
			synchronized (clients) {
				for (Client client : clients.allClients()) {
					client.disconnect();
				}
			}
		}

		@Override
		public void disconnectClient(InetAddress clientAddress) {
			Client client = clients.getClientByAddress(clientAddress);
			client.disconnect();
			removeClient(client);
		}

		private void removeClient(Client client) {
			clients.remove(client);
			visitors.remove(client);
		}

		@Override
		public void start() {
			throw new IllegalStateException();
		}

		@Override
		public State getState() {
			return ClientManager.State.STARTED;
		}

		private void handleMessage(Client client, ClientToServerMessage message) {
			message.accept(visitors.get(client));
		}

		private class MessageListenerCallback implements MessageListener.Callback {
			private final Client client;

			private MessageListenerCallback(Client client) {
				this.client = client;
			}

			@Override
			public void messageReceived(ClientToServerMessage message) {
				handleMessage(client, message);
			}

			@Override
			public void clientDisconnected() {
				removeClient(client);
			}
		}

		private class ClientToServerMessageVisitor implements ClientToServerMessage.Visitor<Void> {
			private final Client client;

			private ClientToServerMessageVisitor(Client client) {
				this.client = client;
			}

			@Override
			public Void visit(ClientVerificationResponse message) {
				// We are not supposed to receive this type of message at this point.
				throw new AssertionError();
			}

			@Override
			public Void visit(TaskMessage message) {
				Task<?> task = message.getTask();
				ClientTaskId clientTaskId = message.getTaskId();
				TaskId taskId = clients.addTask(client, clientTaskId);
				clientManagerCallback.handleTask(taskId, task);
				return null;
			}
		}
	}

	private class StoppedClientManager implements ClientManager {

		@Override
		public void handleResult(TaskId taskId, Result<?> result) {
			throw new IllegalStateException();
		}

		@Override
		public void shutDown() {
			throw new IllegalStateException();
		}

		@Override
		public void disconnectClient(InetAddress clientAddress) {
			throw new IllegalStateException();
		}

		@Override
		public void start() {
			throw new IllegalStateException();
		}

		@Override
		public State getState() {
			return ClientManager.State.STOPPED;
		}
	}

	private static class Clients {
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

		}

		public Client getClient(TaskId id) {
			return clientMap.get(id);
		}

		public Set<Client> allClients() {

		}

		public Client getClientByAddress(InetAddress address) {
			return addressMap.get(address);
		}

		public void remove(Client client) {
			TaskId taskId = clientMap.
		}

		public TaskId addTask(Client client, ClientTaskId clientTaskId) {
			TaskId taskId = idGenerator.newId();
			idMap.put(taskId, clientTaskId);
			clientMap.put(taskId, client);
			return taskId;
		}
	}

	private static class ConnectionAcceptor implements Runnable {
		private final StartedClientManager clientManager;
		private final ConnectionHandler<ServerToClientMessage, ClientToServerMessage> connectionHandler;

		private ConnectionAcceptor(StartedClientManager clientManager,
		                           ConnectionHandler<ServerToClientMessage, ClientToServerMessage> connectionHandler) {
			this.clientManager = clientManager;
			this.connectionHandler = connectionHandler;
		}

		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					clientManager.clientConnected(connectionHandler.accept());
				} catch (CommunicationException e) {
					// TODO: Log this
					throw new AssertionError(e);
				} catch (SocketException e) {
					if (!connectionHandler.isOpen()) {
						break;
					} else {
						// TODO: Log this
						throw new AssertionError(e);
					}
				}
			}
		}
	}

	private static class MessageListener implements Runnable {
		private final Callback callback;
		private final Client client;

		private MessageListener(Callback callback, Client client) {
			this.callback = callback;
			this.client = client;
		}

		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					callback.messageReceived(client.receive());
				} catch (ConnectionClosedException e) {
					callback.clientDisconnected();
					break;
				} catch (CommunicationException e) {
					// TODO: Log this
					client.disconnect();
					callback.clientDisconnected();
					break;
				}
			}
		}

		public interface Callback {
			void messageReceived(ClientToServerMessage message);
			void clientDisconnected();
		}
	}
}