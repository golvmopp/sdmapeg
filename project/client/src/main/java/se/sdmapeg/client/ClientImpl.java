package se.sdmapeg.client;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sdmapeg.client.gui.ClientView;
import se.sdmapeg.common.IdGenerator;
import se.sdmapeg.common.communication.CommunicationException;
import se.sdmapeg.common.communication.Connection;
import se.sdmapeg.common.communication.ConnectionClosedException;
import se.sdmapeg.common.communication.ConnectionImpl;
import se.sdmapeg.common.listeners.Listenable;
import se.sdmapeg.common.listeners.ListenerSupport;
import se.sdmapeg.common.listeners.Notification;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.serverclient.ClientTaskId;
import se.sdmapeg.serverclient.ClientTaskIdGenerator;
import se.sdmapeg.serverclient.communication.*;

public final class ClientImpl implements Client {
	private static final Logger LOG = LoggerFactory.getLogger(ClientImpl.class);
	private static final int SERVER_PORT = 6666;
	private final Listeners listeners;
	private final ExecutorService listenerExecutor;
	private final ExecutorService serverListenerExecutor;
	private final Server server;
	private final String host;
	private final Map<ClientTaskId, Task<?>> taskMap;
	private final Map<ClientTaskId, Result<?>> resultMap;
	private final IdGenerator<ClientTaskId> idGenerator;

	private ClientImpl(String host) throws CommunicationException {
		try {
			Connection<ClientToServerMessage, ServerToClientMessage> connection =
				ConnectionImpl.newConnection(new Socket(host, SERVER_PORT));
			server = ServerImpl.newServer(connection);
		} catch (CommunicationException|IOException e) {
			throw new CommunicationException();
		}
		LOG.info("Connected to {}", server);
		this.listenerExecutor = Executors.newSingleThreadExecutor();
		this.listeners = new Listeners(listenerExecutor);
		serverListenerExecutor = Executors.newSingleThreadExecutor();
		taskMap = new ConcurrentHashMap<>();
		resultMap = new ConcurrentHashMap<>();
		idGenerator = new ClientTaskIdGenerator();
		this.host = host;
	}

	@Override
	public ClientTaskId addTask(Task<?> task) {
		ClientTaskId clientTaskId = idGenerator.newId();
		taskMap.put(clientTaskId, task);
		listeners.taskCreated(clientTaskId);
		return clientTaskId;
	}

	@Override
	public void sendTask(ClientTaskId clientTaskId) {
		server.performTask(clientTaskId, taskMap.get(clientTaskId));
		listeners.taskSent(clientTaskId);
	}

	@Override
	public void start() {
		serverListenerExecutor.execute(new Runnable() {
			@Override
			public void run() {
				server.listen(new ServerEventCallback());
			}
		});
	}

	@Override
	public void cancelTask(ClientTaskId clientTaskId) {
		server.cancelTask(clientTaskId);
		listeners.taskCancelled(clientTaskId);
	}

	@Override
	public void shutDown() {
		server.disconnect();
		serverListenerExecutor.shutdown();
		listenerExecutor.shutdown();
	}

	@Override
	public String getHost() {
		return host;
	}

	private void handleResult(ClientTaskId clientTaskId, Result<?> result) {
		resultMap.put(clientTaskId, result);
		listeners.resultReceived(clientTaskId);
	}

	public Result<?> getResult(ClientTaskId clientTaskId) {
		return resultMap.get(clientTaskId);
	}

	@Override
	public void addListener(ClientListener listener) {
		listeners.addListener(listener);
	}

	@Override
	public void removeListener(ClientListener listener) {
		listeners.removeListener(listener);
	}

	public static Client newClientImp(String host) throws CommunicationException {
		return new ClientImpl(host);
	}

	private final class ServerEventCallback implements ServerCallback {

		@Override
		public void resultReceived(ClientTaskId taskId, Result<?> result) {
			handleResult(taskId, result);
		}

		@Override
		public void connectionClosed() {
			shutDown();
		}
	}

	private static final class Listeners
			implements ClientListener, Listenable<ClientListener> {
		private final ListenerSupport<ClientListener> listenerSupport;

		public Listeners(Executor notificationExecutor) {
			listenerSupport = ListenerSupport.newListenerSupport(
					notificationExecutor);
		}

		@Override
		public void addListener(ClientListener listener) {
			listenerSupport.addListener(listener);
		}

		@Override
		public void removeListener(ClientListener listener) {
			listenerSupport.removeListener(listener);
		}

		@Override
		public void taskCreated(final ClientTaskId clientTaskId) {
			listenerSupport.notifyListeners(new Notification<ClientListener>() {
				@Override
				public void notifyListener(ClientListener listener) {
					listener.taskCreated(clientTaskId);
				}
			});
		}

		@Override
		public void taskSent(final ClientTaskId clientTaskId) {
			listenerSupport.notifyListeners(new Notification<ClientListener>() {
				@Override
				public void notifyListener(ClientListener listener) {
					listener.taskSent(clientTaskId);
				}
			});
		}

		@Override
		public void taskCancelled(final ClientTaskId clientTaskId) {
			listenerSupport.notifyListeners(new Notification<ClientListener>() {
				@Override
				public void notifyListener(ClientListener listener) {
					listener.taskCancelled(clientTaskId);
				}
			});
		}

		@Override
		public void resultReceived(final ClientTaskId clientTaskId) {
			listenerSupport.notifyListeners(new Notification<ClientListener>() {
				@Override
				public void notifyListener(ClientListener listener) {
					listener.resultReceived(clientTaskId);
				}
			});
		}
	}
}
