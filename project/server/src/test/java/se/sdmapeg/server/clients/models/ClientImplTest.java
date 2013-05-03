package se.sdmapeg.server.clients.models;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Test;
import static org.junit.Assert.*;
import se.sdmapeg.common.IdGenerator;
import se.sdmapeg.common.communication.CommunicationException;
import se.sdmapeg.common.communication.Connection;
import se.sdmapeg.common.communication.ConnectionClosedException;
import se.sdmapeg.common.communication.Message;
import se.sdmapeg.common.tasks.FindNextIntTask;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.common.tasks.SimpleFailure;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.server.clients.callbacks.ClientCallback;
import se.sdmapeg.serverclient.ClientTaskId;
import se.sdmapeg.serverclient.ClientTaskIdGenerator;
import se.sdmapeg.serverclient.communication.ClientToServerMessage;
import se.sdmapeg.serverclient.communication.ClientToServerMessage.Handler;
import se.sdmapeg.serverclient.communication.ClientToServerMessageFactory;
import se.sdmapeg.serverclient.communication.ResultMessage;
import se.sdmapeg.serverclient.communication.ServerToClientMessage;
import se.sdmapeg.serverworker.TaskId;
import se.sdmapeg.serverworker.TaskIdGenerator;

/**
 *
 * @author niclas
 */
public class ClientImplTest {
	private static final ClientToServerMessage POISON_MESSAGE = createPoisonMessage();
	public static final InetSocketAddress DUMMY_ADDRESS =
										  InetSocketAddress.createUnresolved("localhost", 1337);
	public ClientImplTest() {
	}

	/**
	 * Test of getAddress method, of class ClientImpl.
	 */
	@Test
	public void testGetAddress() {
		IdGenerator<TaskId> idGenerator = createIdGenerator();
		List<InetSocketAddress> addresses = Arrays.asList(
				new InetSocketAddress[] {
			InetSocketAddress.createUnresolved("localhost", 1337),
			InetSocketAddress.createUnresolved("remotehost", 1337),
			InetSocketAddress.createUnresolved("someotherhost", 1337),
			InetSocketAddress.createUnresolved("192.168.0.1", 1337),
			InetSocketAddress.createUnresolved("127.0.0.1", 1337),
			InetSocketAddress.createUnresolved("www.example.com", 1337),
		});
		List<MockConnection<ServerToClientMessage,
				ClientToServerMessage>> connections =
			createConnections(addresses);
		List<Client> clients = createClients(connections, idGenerator);
		for (PairIterator.Pair<InetSocketAddress, Client> clientAddressPair
				: PairIterator.iterable(addresses, clients)) {
			InetSocketAddress address = clientAddressPair.getLeft();
			Client client = clientAddressPair.getRight();
			assertEquals(address, client.getAddress());
		}
	}

	/**
	 * Test of listen method, of class ClientImpl.
	 */
	@Test
	public void testListen() {
		MockConnection<ServerToClientMessage, ClientToServerMessage> mockConnection =
			mockConnection(DUMMY_ADDRESS);
		ClientTaskIdGenerator clientTaskIdGenerator = new ClientTaskIdGenerator();
		final Task<?> firstTask = FindNextIntTask.newNextIntTask(2);
		final ClientTaskId firstId = clientTaskIdGenerator.newId();
		final Task<?> secondTask = FindNextIntTask.newNextIntTask(3);
		final ClientTaskId secondId = clientTaskIdGenerator.newId();
		mockConnection.addReceived(ClientToServerMessageFactory.newTaskMessage(
				firstTask, firstId));
		mockConnection.addReceived(ClientToServerMessageFactory.newTaskMessage(
				secondTask, secondId));
		mockConnection.addReceived(
				ClientToServerMessageFactory.newTaskCancellationMessage(firstId));
		mockConnection.addReceiveDisconnection();
		ClientCallback callback = new ClientCallback() {
			private final Map<TaskId, Task<?>> taskMap = new HashMap<>();
			private int state = 0;

			@Override
			public void taskReceived(TaskId taskId, Task<?> task) {
				taskMap.put(taskId, task);
				switch (state) {
					case 0:
						assertEquals(firstTask, task);
						break;
					case 1:
						assertEquals(secondTask, task);
						break;
					default:
						fail("Received task at unexpected time");
						break;
				}
				state++;
			}

			@Override
			public void taskCancelled(TaskId taskId) {
				Task<?> task = taskMap.get(taskId);
				switch (state) {
					case 2:
						assertEquals(firstTask, task);
						break;
					default:
						fail("Cancelled task at unexpected time");
						break;
				}
				state++;
			}

			@Override
			public void clientDisconnected() {
				switch (state) {
					case 3:
						break;
					default:
						fail("Client disconnected at unexpected time");
						break;
				}
				state++;
			}
		};
		Client instance = ClientImpl.newClient(mockConnection,
											   createIdGenerator());
		instance.listen(callback);
	}

	@Test
	public void testListenCancelCompletedTask() {
		MockConnection<ServerToClientMessage, ClientToServerMessage> mockConnection =
			mockConnection(DUMMY_ADDRESS);
		ClientTaskIdGenerator clientTaskIdGenerator = new ClientTaskIdGenerator();
		final Task<?> task = FindNextIntTask.newNextIntTask(2);
		final Result<?> result = SimpleFailure.newSimpleFailure(
				new ExecutionException(new AssertionError()));
		final ClientTaskId taskId = clientTaskIdGenerator.newId();
		mockConnection.addReceived(ClientToServerMessageFactory.newTaskMessage(
				task, taskId));
		mockConnection.addReceived(
				ClientToServerMessageFactory.newTaskCancellationMessage(taskId));
		mockConnection.addReceiveDisconnection();
		final Client instance = ClientImpl.newClient(mockConnection,
												   createIdGenerator());
		ClientCallback callback = new ClientCallback() {
			@Override
			public void taskReceived(TaskId taskId, Task<?> task) {
				instance.taskCompleted(taskId, result);
			}

			@Override
			public void taskCancelled(TaskId taskId) {
				fail("The task should already be completed");
			}

			@Override
			public void clientDisconnected() {
			}
		};
		instance.listen(callback);
	}

	@Test
	public void testListenCommunicationError() {
		MockConnection<ServerToClientMessage, ClientToServerMessage> mockConnection =
			mockConnection(DUMMY_ADDRESS);
		mockConnection.addReceiveException(createPoisonMessage(),
			new CommunicationException());
		final Client instance = ClientImpl.newClient(mockConnection,
												   createIdGenerator());
		ClientCallback callback = new ClientCallback() {
			@Override
			public void taskReceived(TaskId taskId, Task<?> task) {
			}

			@Override
			public void taskCancelled(TaskId taskId) {
			}

			@Override
			public void clientDisconnected() {
			}
		};
		instance.listen(callback);
	}

	@Test
	public void testListenDisconnect() {
		MockConnection<ServerToClientMessage, ClientToServerMessage> mockConnection =
			mockConnection(DUMMY_ADDRESS);
		final Client instance = ClientImpl.newClient(mockConnection,
												   createIdGenerator());
		FutureTask<Throwable> listenerTask = new FutureTask<>(
				new Callable<Throwable>() {
			@Override
			public Throwable call() throws Exception {
				try {
					instance.listen(new ClientCallback() {
						@Override
						public void taskReceived(TaskId taskId, Task<?> task) {
						}

						@Override
						public void taskCancelled(TaskId taskId) {
						}

						@Override
						public void clientDisconnected() {
						}
					});
				} catch (Throwable ex) {
					return ex;
				}
				return null;
			}
		});
		new Thread(listenerTask).start();
		instance.disconnect();
		try {
			Throwable exception = listenerTask.get(1, TimeUnit.SECONDS);
			if (exception != null) {
				throw new AssertionError(exception);
			}
		} catch (ExecutionException ex) {
			throw new AssertionError(ex.getCause());
		} catch (TimeoutException ex) {
			listenerTask.cancel(true);
			fail("Disconnecting the client should cause listen to return");
		} catch (InterruptedException ex) {
			listenerTask.cancel(true);
			Thread.currentThread().interrupt();
			fail("Interrupted while running test");
		}
	}

	@Test
	public void testListenReceiveIdentification() {
		MockConnection<ServerToClientMessage, ClientToServerMessage> mockConnection =
			mockConnection(DUMMY_ADDRESS);
		mockConnection.addReceived(ClientToServerMessageFactory
				.newClientIdentificationMessage());
		final Client instance = ClientImpl.newClient(mockConnection,
												   createIdGenerator());
		FutureTask<Throwable> listenerTask = new FutureTask<>(
				new Callable<Throwable>() {
			@Override
			public Throwable call() throws Exception {
				try {
					instance.listen(new ClientCallback() {
						@Override
						public void taskReceived(TaskId taskId, Task<?> task) {
						}

						@Override
						public void taskCancelled(TaskId taskId) {
						}

						@Override
						public void clientDisconnected() {
						}
					});
				} catch (Throwable ex) {
					return ex;
				}
				return null;
			}
		});
		new Thread(listenerTask).start();
		try {
			Throwable exception = listenerTask.get(1, TimeUnit.SECONDS);
			if (exception != null) {
				throw new AssertionError(exception);
			}
		} catch (ExecutionException ex) {
			throw new AssertionError(ex.getCause());
		} catch (TimeoutException ex) {
			listenerTask.cancel(true);
			fail("Receiving an identification message should cause the client"
					+ " to disconnect");
		} catch (InterruptedException ex) {
			listenerTask.cancel(true);
			Thread.currentThread().interrupt();
			fail("Interrupted while running test");
		}
	}

	/**
	 * Test of taskCompleted method, of class ClientImpl.
	 */
	@Test
	public void testTaskCompleted() {
		MockConnection<ServerToClientMessage, ClientToServerMessage> mockConnection =
			mockConnection(DUMMY_ADDRESS);
		ClientTaskIdGenerator clientTaskIdGenerator = new ClientTaskIdGenerator();
		final Task<?> task = FindNextIntTask.newNextIntTask(2);
		final Result<?> result = SimpleFailure.newSimpleFailure(
				new ExecutionException(new AssertionError()));
		final ClientTaskId taskId = clientTaskIdGenerator.newId();
		mockConnection.addReceived(ClientToServerMessageFactory.newTaskMessage(
				task, taskId));
		final Client instance = ClientImpl.newClient(mockConnection,
												   createIdGenerator());
		ClientCallback callback = new ClientCallback() {
			@Override
			public void taskReceived(TaskId taskId, Task<?> task) {
				assertTrue(instance.getActiveTasks().contains(taskId));
				instance.taskCompleted(taskId, result);
				assertFalse(instance.getActiveTasks().contains(taskId));
				instance.disconnect();
			}

			@Override
			public void taskCancelled(TaskId taskId) {
			}

			@Override
			public void clientDisconnected() {
			}
		};
		instance.listen(callback);
		ServerToClientMessage sentMessage = mockConnection.getSent();
		assertNotNull(sentMessage);
		sentMessage.accept(new ServerToClientMessage.Handler<Void>() {
			@Override
			public Void handle(ResultMessage message) {
				assertEquals(taskId, message.getId());
				assertEquals(result, message.getResult());
				return null;
			}
		});
	}

	@Test
	public void testTaskCompletedAfterCancel() {
		final MockConnection<ServerToClientMessage, ClientToServerMessage> mockConnection =
			mockConnection(DUMMY_ADDRESS);
		ClientTaskIdGenerator clientTaskIdGenerator = new ClientTaskIdGenerator();
		final Task<?> task = FindNextIntTask.newNextIntTask(2);
		final Result<?> result = SimpleFailure.newSimpleFailure(
				new ExecutionException(new AssertionError()));
		final ClientTaskId clientTaskId = clientTaskIdGenerator.newId();
		mockConnection.addReceived(ClientToServerMessageFactory.newTaskMessage(
				task, clientTaskId));
		final Client instance = ClientImpl.newClient(mockConnection,
												   createIdGenerator());
		ClientCallback callback = new ClientCallback() {

			@Override
			public void taskReceived(TaskId taskId, Task<?> task) {
				assertTrue(instance.getActiveTasks().contains(taskId));
				mockConnection.addReceived(ClientToServerMessageFactory
						.newTaskCancellationMessage(clientTaskId));
			}

			@Override
			public void taskCancelled(TaskId taskId) {
				assertFalse(instance.getActiveTasks().contains(taskId));
				instance.taskCompleted(taskId, result);
				mockConnection.addReceiveDisconnection();
			}

			@Override
			public void clientDisconnected() {
			}
		};
		instance.listen(callback);
		ServerToClientMessage sentMessage = mockConnection.getSent();
		assertNull(sentMessage);
	}

	@Test
	public void testTaskCompletedConnectionClosed() {
		final MockConnection<ServerToClientMessage, ClientToServerMessage> mockConnection =
			mockConnection(DUMMY_ADDRESS);
		ClientTaskIdGenerator clientTaskIdGenerator = new ClientTaskIdGenerator();
		final Task<?> task = FindNextIntTask.newNextIntTask(2);
		final Result<?> result = SimpleFailure.newSimpleFailure(
				new ExecutionException(new AssertionError()));
		final ClientTaskId clientTaskId = clientTaskIdGenerator.newId();
		mockConnection.addReceived(ClientToServerMessageFactory.newTaskMessage(
				task, clientTaskId));
		final Client instance = ClientImpl.newClient(mockConnection,
												   createIdGenerator());
		ClientCallback callback = new ClientCallback() {

			@Override
			public void taskReceived(TaskId taskId, Task<?> task) {
				assertTrue(instance.getActiveTasks().contains(taskId));
				mockConnection.addSendDisconnection();
				instance.taskCompleted(taskId, result);
			}

			@Override
			public void taskCancelled(TaskId taskId) {
			}

			@Override
			public void clientDisconnected() {
			}
		};
		instance.listen(callback);
		ServerToClientMessage sentMessage = mockConnection.getSent();
		assertNull(sentMessage);
	}

	@Test
	public void testTaskCompletedCommunicationError() {
		final MockConnection<ServerToClientMessage, ClientToServerMessage> mockConnection =
			mockConnection(DUMMY_ADDRESS);
		ClientTaskIdGenerator clientTaskIdGenerator = new ClientTaskIdGenerator();
		final Task<?> task = FindNextIntTask.newNextIntTask(2);
		final Result<?> result = SimpleFailure.newSimpleFailure(
				new ExecutionException(new AssertionError()));
		final ClientTaskId clientTaskId = clientTaskIdGenerator.newId();
		mockConnection.addReceived(ClientToServerMessageFactory.newTaskMessage(
				task, clientTaskId));
		final Client instance = ClientImpl.newClient(mockConnection,
												   createIdGenerator());
		ClientCallback callback = new ClientCallback() {

			@Override
			public void taskReceived(TaskId taskId, Task<?> task) {
				assertTrue(instance.getActiveTasks().contains(taskId));
				mockConnection.addSendException(new CommunicationException());
				instance.taskCompleted(taskId, result);
			}

			@Override
			public void taskCancelled(TaskId taskId) {
			}

			@Override
			public void clientDisconnected() {
			}
		};
		instance.listen(callback);
		ServerToClientMessage sentMessage = mockConnection.getSent();
		assertNull(sentMessage);
	}

	/**
	 * Test of getActiveTasks method, of class ClientImpl.
	 */
	@Test
	public void testGetActiveTasks() {
		MockConnection<ServerToClientMessage, ClientToServerMessage> mockConnection =
			mockConnection(DUMMY_ADDRESS);
		ClientTaskIdGenerator clientTaskIdGenerator = new ClientTaskIdGenerator();
		final Client instance = ClientImpl.newClient(mockConnection,
													 new TaskIdGenerator());
		Set<Set<TaskId>> taskSets = new HashSet<>();
		Set<TaskId> noActiveTasks = instance.getActiveTasks();
		assertTrue(noActiveTasks.isEmpty());
		final Result<?> result = SimpleFailure.newSimpleFailure(
				new ExecutionException(new AssertionError()));
		final Task<?> firstTask = FindNextIntTask.newNextIntTask(2);
		final ClientTaskId firstId = clientTaskIdGenerator.newId();
		final Task<?> secondTask = FindNextIntTask.newNextIntTask(3);
		final ClientTaskId secondId = clientTaskIdGenerator.newId();
		final Task<?> thirdTask = FindNextIntTask.newNextIntTask(6);
		final ClientTaskId thirdId = clientTaskIdGenerator.newId();
		mockConnection.addReceived(ClientToServerMessageFactory.newTaskMessage(
				firstTask, firstId));
		mockConnection.addReceived(ClientToServerMessageFactory.newTaskMessage(
				secondTask, secondId));
		mockConnection.addReceived(ClientToServerMessageFactory.newTaskMessage(
				thirdTask, thirdId));
		mockConnection.addReceiveDisconnection();
		instance.listen(new ClientCallback() {
			private Set<TaskId> memory = new HashSet<>();
			private int state = 0;
			@Override
			public void taskReceived(TaskId taskId, Task<?> task) {
				Set<TaskId> activeTasks = instance.getActiveTasks();
				assertTrue(activeTasks.contains(taskId));
				switch (state) {
					case 0:
						assertEquals(1, activeTasks.size());
						instance.taskCompleted(taskId, result);
						activeTasks = instance.getActiveTasks();
						assertTrue(activeTasks.isEmpty());
						break;
					case 1:
						assertEquals(1, activeTasks.size());
						memory.add(taskId);
						break;
					case 2:
						assertEquals(2, activeTasks.size());
						for (TaskId memoryId : memory) {
							instance.taskCompleted(memoryId, result);
						}
						activeTasks = instance.getActiveTasks();
						assertEquals(1, activeTasks.size());
						assertTrue(activeTasks.contains(taskId));
						for (TaskId memoryId : memory) {
							assertFalse(activeTasks.contains(memoryId));
						}
						memory.clear();
						break;
					default:
						fail("Unexpected state");
						break;
				}
				state++;
			}

			@Override
			public void taskCancelled(TaskId taskId) {
			}

			@Override
			public void clientDisconnected() {
			}
		});
	}

	/**
	 * Test of disconnect method, of class ClientImpl.
	 */
	@Test
	public void testDisconnect() {
		final MockConnection<ServerToClientMessage, ClientToServerMessage> mockConnection =
			mockConnection(DUMMY_ADDRESS);
		final Client instance = ClientImpl.newClient(mockConnection,
													 createIdGenerator());
		instance.disconnect();
	}

	@Test
	public void testDisconnectException() {
		final MockConnection<ServerToClientMessage, ClientToServerMessage> mockConnection =
			mockConnection(DUMMY_ADDRESS);
		mockConnection.setExceptionOnClose(true);
		final Client instance = ClientImpl.newClient(mockConnection,
													 createIdGenerator());
		instance.disconnect();
	}

	private static IdGenerator<TaskId> createIdGenerator() {
		return new TaskIdGenerator();
	}

	private static MockConnection<ServerToClientMessage, ClientToServerMessage>
			mockConnection(InetSocketAddress address) {
		return new MockConnection<ServerToClientMessage, ClientToServerMessage>(
				address, POISON_MESSAGE);
	}

	private static ClientToServerMessage createPoisonMessage() {
		return new ClientToServerMessage() {
			@Override
			public <T> T accept(Handler<T> handler) {
				throw new UnsupportedOperationException();
			}
		};
	}

	private List<MockConnection<ServerToClientMessage, ClientToServerMessage>>
			createConnections(List<InetSocketAddress> addresses) {
		List<MockConnection<ServerToClientMessage, ClientToServerMessage>> connections =
			new ArrayList<>(addresses.size());
		for (InetSocketAddress address : addresses) {
			connections.add(mockConnection(address));
		}
		return Collections.unmodifiableList(connections);
	}

	private List<Client> createClients(
			List<? extends Connection<ServerToClientMessage,
					ClientToServerMessage>> connections,
			IdGenerator<TaskId> createIdGenerator) {
		List<Client> clients = new ArrayList<>(connections.size());
		for (Connection<ServerToClientMessage, ClientToServerMessage> mockConnection :
			 connections) {
			clients.add(ClientImpl.newClient(mockConnection, createIdGenerator));
		}
		return clients;
	}

	private static final class MockConnection<S extends Message,
			R extends Message> implements Connection<S, R> {
		private final InetSocketAddress address;
		private final R closePoision;
		private final Map<R, CommunicationException> receivePoison =
			new ConcurrentHashMap<>();
		private final BlockingDeque<R> input = new LinkedBlockingDeque<>();
		private final BlockingQueue<S> output = new LinkedBlockingQueue<>();
		private final BlockingQueue<CommunicationException> sendPoison =
			new LinkedBlockingQueue<>();
		private volatile boolean exceptionOnClose = false;
		private volatile boolean closed = false;

		public MockConnection(InetSocketAddress address, R closePoision) {
			this.address = address;
			this.closePoision = closePoision;
		}

		@Override
		public InetSocketAddress getAddress() {
			return address;
		}

		@Override
		public void send(S message) throws CommunicationException,
										   ConnectionClosedException {
			if (!isOpen()) {
				throw new ConnectionClosedException();
			}
			checkSendPoison();
			output.add(message);
		}

		@Override
		public R receive() throws CommunicationException,
								  ConnectionClosedException {
			if (!isOpen()) {
				throw new ConnectionClosedException();
			}
			try {
				R message = input.take();
				checkReceivePoison(message);
				return message;
			} catch (InterruptedException ex) {
				throw new ConnectionClosedException();
			}
		}

		@Override
		public boolean isOpen() {
			return !closed;
		}

		@Override
		public void close() throws IOException {
			closed = true;
			receivePoison.put(closePoision, new ConnectionClosedException());
			input.push(closePoision);
			if (exceptionOnClose) {
				throw new IOException();
			}
		}

		public void addReceived(R message) {
			input.add(message);
		}

		public void addReceiveException(R poison, CommunicationException exeption) {
			receivePoison.put(poison, exeption);
			addReceived(poison);
		}

		public void addReceiveDisconnection() {
			addReceiveException(closePoision, new ConnectionClosedException());
		}

		public void addSendException(CommunicationException exception) {
			sendPoison.add(exception);
		}

		public void addSendDisconnection() {
			addSendException(new ConnectionClosedException());
		}

		public S getSent() {
			return output.poll();
		}

		public void setExceptionOnClose(boolean fail) {
			this.exceptionOnClose = fail;
		}

		private void checkReceivePoison(R message) throws CommunicationException {
			CommunicationException exception = receivePoison.remove(message);
			if (exception != null) {
				throw exception;
			}
		}

		private void checkSendPoison() throws CommunicationException {
			CommunicationException exception = sendPoison.poll();
			if (exception != null) {
				throw exception;
			}
		}
	}

	private static final class PairIterator<L, R> implements Iterator<PairIterator.Pair<L, R>> {
		private final Iterator<L> leftIterator;
		private final Iterator<R> rightIterator;

		public PairIterator(Iterator<L> leftIterator,
							Iterator<R> rightIterator) {
			this.leftIterator = leftIterator;
			this.rightIterator = rightIterator;
		}

		@Override
		public boolean hasNext() {
			return leftIterator.hasNext() && rightIterator.hasNext();
		}

		@Override
		public Pair<L, R> next() {
			return new PairIterator.Pair<>(leftIterator.next(),
					rightIterator.next());
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		public static <L, R> Iterable<PairIterator.Pair<L, R>> iterable(
				final Iterable<L> leftIterable, final Iterable<R> rightIterable) {
			return new Iterable<PairIterator.Pair<L, R>>() {
						@Override
						public Iterator<Pair<L, R>> iterator() {
							return new PairIterator<>(leftIterable.iterator(),
									rightIterable.iterator());
						}
					};
		}

		public static final class Pair<L, R> {
			private final L left;
			private final R right;

			public Pair(L left, R right) {
				this.right = right;
				this.left = left;
			}

			public L getLeft() {
				return left;
			}

			public R getRight() {
				return right;
			}
		}
	}
}
