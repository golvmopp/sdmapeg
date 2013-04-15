package se.sdmapeg.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientImpl implements Client {
	private static final Logger LOG = LoggerFactory.getLogger(ClientImpl.class);
	private final ExecutorService executorService;
	private final Server server;
	private final Map<ClientTaskId, Task<?>> taskMap;
	private final Map<ClientTaskId, Result<?>> resultMap;
	private final IdGenerator<ClientTaskId> idGenerator;

	private ClientImpl() {
		try {
			server = ServerImpl.newServer(ConnectionImpl.newConnection(new Socket()));
		} catch (CommunicationException e) {
			// TODO: throw a better exception
			throw new AssertionError(e);
		}
		executorService = Executors.newCachedThreadPool();
		taskMap = new ConcurrentHashMap<>();
		resultMap = new ConcurrentHashMap<>();
		idGenerator = new ClientTaskIdGenerator();
	}

	public void addTask(Task task) {
		ClientTaskId id = idGenerator.newId();
		taskMap.put(id, task);
	}

	public void sendTask(ClientTaskId id) {
		try {
			server.send(TaskMessage.newTaskMessage(taskMap.get(id), id));
		} catch (CommunicationException e) {
			LOG.warn("Connection was closed before this task could be sent.");
		}
	}

	public void receive() {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				try {
					while (!Thread.currentThread().isInterrupted()) {
						ServerToClientMessage message = server.receive();
						message.accept(new ServerMessageHandler());
					}
				} catch (ConnectionClosedException e) {
					LOG.info("Connection to server was closed.");
				} catch (CommunicationException e) {
					LOG.error("An error occurred while listening for messages.", e);
				}
			}
		});
	}

	private void handleResult(ClientTaskId id, Result<?> result) {
		resultMap.put(id, result);
		showResult(id);
	}

	private void showResult(ClientTaskId id) {
	}

	public static Client newClientImp() {
		return new ClientImpl();
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
