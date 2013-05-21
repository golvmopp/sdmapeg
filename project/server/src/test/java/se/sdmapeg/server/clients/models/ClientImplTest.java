package se.sdmapeg.server.clients.models;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import static org.junit.Assert.*;
import org.junit.Test;
import se.sdmapeg.common.IdGenerator;
import se.sdmapeg.common.communication.CommunicationException;
import se.sdmapeg.common.communication.Connection;
import se.sdmapeg.common.tasks.FindNextIntTask;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.common.tasks.SimpleFailure;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.server.test.MockConnection;
import se.sdmapeg.server.test.PairIterator;
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
	private static final InetSocketAddress DUMMY_ADDRESS =
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
		Task<?> firstTask = FindNextIntTask.newNextIntTask(2);
		ClientTaskId firstId = clientTaskIdGenerator.newId();
		Task<?> secondTask = FindNextIntTask.newNextIntTask(3);
		ClientTaskId secondId = clientTaskIdGenerator.newId();
		Client instance = ClientImpl.newClient(mockConnection,
			createIdGenerator());
		new ClientInteractionTester(instance, mockConnection)
			.addReceived(ClientToServerMessageFactory.newTaskMessage(
				firstTask, firstId))
			.expectTaskReceived(firstTask)
			.addReceived(ClientToServerMessageFactory.newTaskMessage(
				secondTask, secondId))
			.expectTaskReceived(secondTask)
			.addReceived(ClientToServerMessageFactory
				.newTaskCancellationMessage(firstId))
			.expectTaskCancelled(firstTask)
			.addReceiveDisconnection()
			.expectDisconnection()
			.runTest();
	}

	@Test
	public void testListenCancelCompletedTask() {
		MockConnection<ServerToClientMessage, ClientToServerMessage> mockConnection =
			mockConnection(DUMMY_ADDRESS);
		ClientTaskIdGenerator clientTaskIdGenerator = new ClientTaskIdGenerator();
		Task<?> task = FindNextIntTask.newNextIntTask(2);
		Result<?> result = SimpleFailure.newSimpleFailure(
				new ExecutionException(new AssertionError()));
		Client instance = ClientImpl.newClient(mockConnection,
			createIdGenerator());
		ClientTaskId taskId = clientTaskIdGenerator.newId();
		new ClientInteractionTester(instance, mockConnection)
			.addReceived(ClientToServerMessageFactory.newTaskMessage(
				task, taskId))
			.expectTaskReceived(task)
			.addTaskCompleted(task, result)
			.addReceived(
				ClientToServerMessageFactory.newTaskCancellationMessage(taskId))
			.addReceiveDisconnection()
			.expectDisconnection()
			.runTest();
	}

	@Test
	public void testListenCommunicationError() {
		MockConnection<ServerToClientMessage, ClientToServerMessage> mockConnection =
			mockConnection(DUMMY_ADDRESS);
		Client instance = ClientImpl.newClient(mockConnection,
			createIdGenerator());
		new ClientInteractionTester(instance, mockConnection)
			.addReceiveException(createPoisonMessage(),
				new CommunicationException())
			.expectDisconnection()
			.runTest();
	}

	@Test
	public void testListenDisconnect() {
		MockConnection<ServerToClientMessage, ClientToServerMessage> mockConnection =
			mockConnection(DUMMY_ADDRESS);
		Client instance = ClientImpl.newClient(mockConnection,
			createIdGenerator());
		FutureTask<Throwable> listenerTask =
			new FutureTask<>(new ClientCallbackListener(instance));
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
		Client instance = ClientImpl.newClient(mockConnection,
			createIdGenerator());
		FutureTask<Throwable> listenerTask = new FutureTask<>(
				new ClientCallbackListener(instance));
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
		Task<?> task = FindNextIntTask.newNextIntTask(2);
		final Result<?> result = SimpleFailure.newSimpleFailure(
				new ExecutionException(new AssertionError()));
		final ClientTaskId taskId = clientTaskIdGenerator.newId();
		final Client instance = ClientImpl.newClient(mockConnection,
			createIdGenerator());
		new ClientInteractionTester(instance, mockConnection)
			.addReceived(ClientToServerMessageFactory.newTaskMessage(
				task, taskId))
			.expectTaskReceived(task)
			.addTaskCompleted(task, result)
			.addReceiveDisconnection()
			.expectDisconnection()
			.runTest();
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
		MockConnection<ServerToClientMessage, ClientToServerMessage> mockConnection =
			mockConnection(DUMMY_ADDRESS);
		ClientTaskIdGenerator clientTaskIdGenerator = new ClientTaskIdGenerator();
		Task<?> task = FindNextIntTask.newNextIntTask(2);
		Result<?> result = SimpleFailure.newSimpleFailure(
				new ExecutionException(new AssertionError()));
		ClientTaskId clientTaskId = clientTaskIdGenerator.newId();
		Client instance = ClientImpl.newClient(mockConnection,
			createIdGenerator());
		new ClientInteractionTester(instance, mockConnection)
			.addReceived(ClientToServerMessageFactory.newTaskMessage(task,
				clientTaskId))
			.expectTaskReceived(task)
			.addReceived(ClientToServerMessageFactory
				.newTaskCancellationMessage(clientTaskId))
			.expectTaskCancelled(task)
			.addTaskCompleted(task, result)
			.addReceiveDisconnection()
			.expectDisconnection()
			.runTest();
		ServerToClientMessage sentMessage = mockConnection.getSent();
		assertNull(sentMessage);
	}

	@Test
	public void testTaskCompletedConnectionClosed() {
		MockConnection<ServerToClientMessage, ClientToServerMessage> mockConnection =
			mockConnection(DUMMY_ADDRESS);
		ClientTaskIdGenerator clientTaskIdGenerator = new ClientTaskIdGenerator();
		Task<?> task = FindNextIntTask.newNextIntTask(2);
		Result<?> result = SimpleFailure.newSimpleFailure(
				new ExecutionException(new AssertionError()));
		ClientTaskId clientTaskId = clientTaskIdGenerator.newId();
		Client instance = ClientImpl.newClient(mockConnection,
			createIdGenerator());
		new ClientInteractionTester(instance, mockConnection)
			.addReceived(ClientToServerMessageFactory.newTaskMessage(task,
				clientTaskId))
			.expectTaskReceived(task)
			.addSendDisconnection()
			.addTaskCompleted(task, result)
			.expectDisconnection()
			.runTest();
		ServerToClientMessage sentMessage = mockConnection.getSent();
		assertNull(sentMessage);
	}

	@Test
	public void testTaskCompletedCommunicationError() {
		MockConnection<ServerToClientMessage, ClientToServerMessage> mockConnection =
			mockConnection(DUMMY_ADDRESS);
		ClientTaskIdGenerator clientTaskIdGenerator = new ClientTaskIdGenerator();
		Task<?> task = FindNextIntTask.newNextIntTask(2);
		Result<?> result = SimpleFailure.newSimpleFailure(
				new ExecutionException(new AssertionError()));
		ClientTaskId clientTaskId = clientTaskIdGenerator.newId();
		Client instance = ClientImpl.newClient(mockConnection,
			createIdGenerator());
		new ClientInteractionTester(instance, mockConnection)
			.addReceived(ClientToServerMessageFactory.newTaskMessage(task,
				clientTaskId))
			.expectTaskReceived(task)
			.addSendException(new CommunicationException())
			.addTaskCompleted(task, result)
			.expectDisconnection()
			.runTest();
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
		Client instance = ClientImpl.newClient(mockConnection,
													 new TaskIdGenerator());
		Set<TaskId> noActiveTasks = instance.getActiveTasks();
		assertTrue(noActiveTasks.isEmpty());
		Result<?> result = SimpleFailure.newSimpleFailure(
				new ExecutionException(new AssertionError()));
		Task<?> firstTask = FindNextIntTask.newNextIntTask(2);
		ClientTaskId firstId = clientTaskIdGenerator.newId();
		Task<?> secondTask = FindNextIntTask.newNextIntTask(3);
		ClientTaskId secondId = clientTaskIdGenerator.newId();
		Task<?> thirdTask = FindNextIntTask.newNextIntTask(6);
		ClientTaskId thirdId = clientTaskIdGenerator.newId();
		new ClientInteractionTester(instance, mockConnection)
			.addReceived(ClientToServerMessageFactory.newTaskMessage(firstTask,
				firstId))
			.addReceived(ClientToServerMessageFactory.newTaskMessage(secondTask,
				secondId))
			.addReceived(ClientToServerMessageFactory.newTaskMessage(thirdTask,
				thirdId))
			.expectTaskReceived(firstTask)
			.addTaskCompleted(firstTask, result)
			.expectTaskReceived(secondTask)
			.expectTaskReceived(thirdTask)
			.addReceiveDisconnection()
			.expectDisconnection()
			.runTest();
	}

	/**
	 * Test of disconnect method, of class ClientImpl.
	 */
	@Test
	public void testDisconnect() {
		MockConnection<ServerToClientMessage, ClientToServerMessage> mockConnection =
			mockConnection(DUMMY_ADDRESS);
		Client instance = ClientImpl.newClient(mockConnection,
			createIdGenerator());
		instance.disconnect();
	}

	@Test
	public void testDisconnectException() {
		MockConnection<ServerToClientMessage, ClientToServerMessage> mockConnection =
			mockConnection(DUMMY_ADDRESS);
		mockConnection.setExceptionOnClose(true);
		Client instance = ClientImpl.newClient(mockConnection,
			createIdGenerator());
		instance.disconnect();
	}

	private static IdGenerator<TaskId> createIdGenerator() {
		return new TaskIdGenerator();
	}

	private static MockConnection<ServerToClientMessage, ClientToServerMessage>
			mockConnection(InetSocketAddress address) {
		return new MockConnection<>(address, POISON_MESSAGE);
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
}
