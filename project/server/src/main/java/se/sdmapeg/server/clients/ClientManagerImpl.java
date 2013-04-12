package se.sdmapeg.server.clients;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sdmapeg.common.IdGenerator;
import se.sdmapeg.common.communication.CommunicationException;
import se.sdmapeg.common.communication.ConnectionClosedException;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.server.communication.ConnectionHandler;
import se.sdmapeg.serverclient.ClientTaskId;
import se.sdmapeg.serverclient.communication.*;
import se.sdmapeg.serverworker.TaskId;

/**
 * Implementation of ClientManager interface.
 */
public final class ClientManagerImpl implements ClientManager {
	private static final Logger LOG = LoggerFactory.getLogger(ClientManagerImpl.class);
	private final AtomicReference<ClientManagerState> state;
	private final ClientManagerCallback callback;
	private final ExecutorService connectionThreadPool;
	private final ConnectionHandler<ServerToClientMessage,
			ClientToServerMessage> clientConnectionHandler;
	private final Clients clients;

	public ClientManagerImpl(ClientManagerCallback callback,
							 ExecutorService connectionThreadPool,
							 ConnectionHandler<ServerToClientMessage,
									 ClientToServerMessage>
							 clientConnectionHandler,
							 IdGenerator<TaskId> taskIdGenerator) {
		this.callback = callback;
		this.connectionThreadPool = connectionThreadPool;
		this.clientConnectionHandler = clientConnectionHandler;
		this.clients = new Clients(taskIdGenerator);
		this.state = new AtomicReference<ClientManagerState>(new CreatedState());
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

	private void disconnectClient(Client client) {
		client.disconnect();
		clients.remove(client);
	}

	private void clientConnected(Client client) {
		state.get().clientConnected(client);
	}

	private void handleTask(Task<?> task, ClientTaskId clientTaskId,
										  Client source) {
		state.get().handleTask(task, clientTaskId, source);
	}

	private final class CreatedState implements ClientManagerState {

		@Override
		public void initiate() {
			// Do nothing
		}

		@Override
		public void clientConnected(Client client) {
			ClientManagerImpl.this.disconnectClient(client);
			throw new IllegalStateException();
		}

		@Override
		public void handleTask(Task<?> task, ClientTaskId clientTaskId,
							   Client source) {
			throw new IllegalStateException();
		}

		@Override
		public void handleResult(TaskId taskId, Result<?> result) {
			throw new IllegalStateException();
		}

		@Override
		public void shutDown() {
			ClientManagerState shutdownState = new ShutdownState();
			if (state.compareAndSet(this, shutdownState)) {
				shutdownState.initiate();
			} else {
				ClientManagerImpl.this.shutDown();
			}
		}

		@Override
		public void disconnectClient(InetAddress clientAddress) {
			throw new IllegalStateException();
		}

		@Override
		public void start() {
			ClientManagerState startedState = new StartedState();
			if (state.compareAndSet(this, startedState)) {
				startedState.initiate();
			} else {
				ClientManagerImpl.this.start();
			}
		}

		@Override
		public State getState() {
			return State.CREATED;
		}
	}

	private final class StartedState implements ClientManagerState {

		@Override
		public void initiate() {
			connectionThreadPool.submit(new ClientConnectionAcceptor(
					clientConnectionHandler, new ConnectionAcceptorCallback()));
		}

		@Override
		public void clientConnected(Client client) {
			clients.addClient(client);
			connectionThreadPool.submit(new ClientMessageListener(client,
					new ClientMessageHandler(client),
					new ClientDisconnectionHandler(client)));
		}

		@Override
		public void handleTask(Task<?> task, ClientTaskId clientTaskId,
							   Client source) {
			TaskId taskId = clients.addTask(source, clientTaskId);
			if (taskId == null) {
				return;
			}
			callback.handleTask(taskId, task);
		}

		@Override
		public void handleResult(TaskId taskId, Result<?> result) {
			Client client = clients.getClientByTaskId(taskId);
			ClientTaskId clientTaskId = clients.getClientTaskId(taskId);
			if (client == null || clientTaskId == null) {
				LOG.warn("Received result for task with id {} but found no"
						+ " client waiting for it", taskId);
				return;
			}
			ResultMessage resultMessage =
				ResultMessage.newResultMessage(clientTaskId, result);
			try {
				sendMessage(client, resultMessage);
			}
			catch (CommunicationException ex) {
				LOG.warn("Failed to send result for task with id " + taskId
						 + " to " +  client, ex);
			}
		}

		@Override
		public void shutDown() {
			ClientManagerState shutdownState = new ShutdownState();
			if (state.compareAndSet(this, shutdownState)) {
				shutdownState.initiate();
			} else {
				ClientManagerImpl.this.shutDown();
			}
		}

		@Override
		public void disconnectClient(InetAddress clientAddress) {
			Client client = clients.getClientByAddress(clientAddress);
			if (client == null) {
				return;
			}
			ClientManagerImpl.this.disconnectClient(client);
			LOG.info("{} was disconnected", client);
		}

		@Override
		public void start() {
			// Do nothing
		}

		@Override
		public State getState() {
			return State.STARTED;
		}

		private void sendMessage(Client client, ServerToClientMessage message)
				throws CommunicationException {
			try {
				client.send(message);
			} catch (ConnectionClosedException ex) {
				ClientManagerImpl.this.disconnectClient(client);
				LOG.info("{} was disconnected", client);
			}
		}
	}

	private final class ShutdownState implements ClientManagerState {

		@Override
		public void initiate() {
			try {
				clientConnectionHandler.close();
			} catch (IOException ex) {
				LOG.warn("An error occurred while shutting down the client"
						+ " manager", ex);
			}
		}

		@Override
		public void clientConnected(Client client) {
			ClientManagerImpl.this.disconnectClient(client);
		}

		@Override
		public void handleTask(Task<?> task, ClientTaskId clientTaskId,
							   Client source) {
			// Do nothing
		}

		@Override
		public void handleResult(TaskId taskId,
								 Result<?> result) {
			// Do nothing
		}

		@Override
		public void shutDown() {
			// Do nothing
		}

		@Override
		public void disconnectClient(InetAddress clientAddress) {
			// Do nothing
		}

		@Override
		public void start() {
			// Do nothing
		}

		@Override
		public State getState() {
			return State.STOPPED;
		}		
	}

	private interface ClientManagerState extends ClientManager {
		void initiate();
		void clientConnected(Client client);
		void handleTask(Task<?> task, ClientTaskId clientTaskId, Client source);
	}

	private final class ConnectionAcceptorCallback
			implements ClientConnectionAcceptor.Callback {

		@Override
		public void clientConnected(Client client) {
			ClientManagerImpl.this.clientConnected(client);
		}

		@Override
		public void shutdown() {
			for (Client client : clients.allClients()) {
				disconnectClient(client);
			}
		}
	}

	private final class ClientMessageHandler implements ClientToServerMessage.Handler<Void> {
		private final Client client;

		public ClientMessageHandler(Client client) {
			this.client = client;
		}

		@Override
		public Void handle(ClientIdentification message) {
			throw new IllegalStateException();
		}

		@Override
		public Void handle(TaskMessage message) {
			handleTask(message.getTask(), message.getTaskId(), client);
			return null;
		}
	}

	private final class ClientDisconnectionHandler
			implements ClientMessageListener.DisconnectionCallback {
		private final Client client;

		public ClientDisconnectionHandler(Client client) {
			this.client = client;
		}

		@Override
		public void clientDisconnected() {
			disconnectClient(client);
			LOG.info("{} was disconnected", client);
		}
	}
}
