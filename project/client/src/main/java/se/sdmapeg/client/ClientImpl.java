package se.sdmapeg.client;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sdmapeg.client.gui.ClientView;
import se.sdmapeg.common.IdGenerator;
import se.sdmapeg.common.communication.CommunicationException;
import se.sdmapeg.common.communication.ConnectionClosedException;
import se.sdmapeg.common.communication.ConnectionImpl;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.serverclient.ClientTaskId;
import se.sdmapeg.serverclient.ClientTaskIdGenerator;
import se.sdmapeg.serverclient.communication.ResultMessage;
import se.sdmapeg.serverclient.communication.ServerToClientMessage;
import se.sdmapeg.serverclient.communication.TaskMessage;

public class ClientImpl implements Client {
	private static final Logger LOG = LoggerFactory.getLogger(ClientImpl.class);
	private static final int SERVER_PORT = 6666;
	private final ExecutorService serverListenerExecutor;
	private final Server server;
	private final Map<ClientTaskId, Task<?>> taskMap;
	private final Map<ClientTaskId, Result<?>> resultMap;
	private final IdGenerator<ClientTaskId> idGenerator;
	private final ClientView view;

	private ClientImpl(ClientView view, String host) throws CommunicationException {
		try {
			server = ServerImpl.newServer(ConnectionImpl.newConnection(new Socket(host, SERVER_PORT)));
		} catch (CommunicationException|IOException e) {
			throw new CommunicationException();
		}
		serverListenerExecutor = Executors.newSingleThreadExecutor();
		taskMap = new ConcurrentHashMap<>();
		resultMap = new ConcurrentHashMap<>();
		idGenerator = new ClientTaskIdGenerator();
		this.view = view;
		view.show(this);
	}

	@Override
	public ClientTaskId addTask(Task task) {
		ClientTaskId id = idGenerator.newId();
		taskMap.put(id, task);
		return id;
	}

	@Override
	public void sendTask(ClientTaskId id) {
		try {
			server.send(TaskMessage.newTaskMessage(taskMap.get(id), id));
		} catch (CommunicationException e) {
			LOG.warn("Connection was closed before this task could be sent.");
		}
	}

	@Override
	public void start() {
		serverListenerExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					while (true) {
						ServerToClientMessage message = server.receive();
						message.accept(new ServerMessageHandler());
					}
				} catch (ConnectionClosedException e) {
					LOG.info("Connection to server was closed.");
				} catch (CommunicationException e) {
					LOG.error("An error occurred while listening for messages.", e);
				} finally {
					view.showConnectionError();
				}
			}
		});
	}

	@Override
	public void shutDown() {
		server.disconnect();
		serverListenerExecutor.shutdown();
	}

	private void handleResult(ClientTaskId id, Result<?> result) {
		resultMap.put(id, result);
		showResult(id);
	}

	private void showResult(ClientTaskId id) {
		view.showResult(resultMap.get(id));
	}

	public static Client newClientImp(ClientView view, String host) throws CommunicationException {
		return new ClientImpl(view, host);
	}

	private final class ServerMessageHandler implements ServerToClientMessage.Handler<Void> {
		@Override
		public Void handle(ResultMessage message) {
			ClientTaskId id = message.getId();
			Result<?> result = message.getResult();
			handleResult(id, result);
			return null;
		}
	}
}
