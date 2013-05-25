package se.sdmapeg.server.clients.models;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.hamcrest.core.IsEqual;
import static org.junit.Assert.*;
import org.junit.Test;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.server.clients.callbacks.ClientManagerCallback;
import se.sdmapeg.server.clients.callbacks.ClientManagerListener;
import se.sdmapeg.server.clients.callbacks.ClientManagerListenerSupport;
import se.sdmapeg.server.clients.exceptions.ClientRejectedException;
import se.sdmapeg.server.test.CurrentThreadExecutor;
import se.sdmapeg.serverworker.TaskId;
import se.sdmapeg.serverworker.TaskIdGenerator;

/**
 *
 * @author niclas
 */
public class ClientManagerModelTest {
	private static final ClientManagerCallback EMPTY_CALLBACK =
			new ClientManagerCallback() {
		@Override
		public void handleTask(TaskId taskId, Task<?> task) {
		}

		@Override
		public void cancelTask(TaskId taskId) {
		}
	};

	public ClientManagerModelTest() {
	}

	/**
	 * Test of addClient method, of class ClientManagerModel.
	 */
	@Test
	public void testAddClient() throws Exception {
		final Client client = new MockClient(InetSocketAddress.createUnresolved(
				"localhost", 1337));
		ClientManagerCallback callback = EMPTY_CALLBACK;
		ClientManagerModel instance = createClientManagerModel(callback);
		instance.addClient(client);
		Set<Client> clients = instance.getClients();
		assertEquals(1, clients.size());
		assertTrue(clients.contains(client));
	}

	@Test
	public void testAddClientNotifyListener() throws Exception {
		final Client client = new MockClient(InetSocketAddress.createUnresolved(
				"localhost", 1337));
		ClientManagerCallback callback = EMPTY_CALLBACK;
		ClientManagerModel instance = createClientManagerModel(callback);
		SpecificNotificationListener listener =
			SpecificNotificationListener.clientConnected(
				IsEqual.equalTo(client.getAddress()));
		instance.addListener(listener);
		instance.addClient(client);
		assertTrue(listener.wasNotified());
	}

	@Test
	public void testAddClientAlreadyPresent() throws Exception {
		InetSocketAddress address = InetSocketAddress.createUnresolved(
				"localhost", 1337);
		final Client client = new MockClient(address);
		ClientManagerCallback callback = EMPTY_CALLBACK;
		ClientManagerModel instance = createClientManagerModel(callback);
		instance.addClient(client);
		try {
			instance.addClient(client);
			fail("Clients already present should be rejected");
		} catch (ClientRejectedException ex) {
			// success
		}
		Set<Client> clients = instance.getClients();
		assertEquals(1, clients.size());
		assertTrue(clients.contains(client));
	}

	@Test
	public void testAddClientDuplicate() throws Exception {
		InetSocketAddress address = InetSocketAddress.createUnresolved(
				"localhost", 1337);
		final Client firstClient = new MockClient(address);
		ClientManagerCallback callback = EMPTY_CALLBACK;
		ClientManagerModel instance = createClientManagerModel(callback);
		instance.addClient(firstClient);
		MockClient secondClient = new MockClient(address);
		try {
			instance.addClient(secondClient);
			fail("Dupliacte clients should be rejected");
		} catch (ClientRejectedException ex) {
			// success
		}
		Set<Client> clients = instance.getClients();
		assertEquals(1, clients.size());
		assertTrue(clients.contains(firstClient));
	}

	@Test
	public void testAddClientAfterRemoval() throws Exception {
		InetSocketAddress address = InetSocketAddress.createUnresolved(
				"localhost", 1337);
		final Client firstClient = new MockClient(address);
		ClientManagerCallback callback = EMPTY_CALLBACK;
		ClientManagerModel instance = createClientManagerModel(callback);
		instance.addClient(firstClient);
		MockClient secondClient = new MockClient(address);
		instance.removeClient(firstClient);
		try {
			instance.addClient(secondClient);
		} catch (ClientRejectedException ex) {
			fail("Adding a new client with the same address after removing the"
					+ " old one should not fail");
		}
		Set<Client> clients = instance.getClients();
		assertEquals(1, clients.size());
		assertTrue(clients.contains(secondClient));
	}

	/**
	 * Test of getClient method, of class ClientManagerModel.
	 */
	@Test
	public void testGetClient() throws Exception {
		InetSocketAddress address = InetSocketAddress.createUnresolved(
				"localhost", 1337);
		final Client client = new MockClient(address);
		ClientManagerCallback callback = EMPTY_CALLBACK;
		ClientManagerModel instance = createClientManagerModel(callback);
		instance.addClient(client);
		assertSame(client, instance.getClient(address));
	}

	@Test
	public void testGetClientNotPresent() {
		InetSocketAddress address = InetSocketAddress.createUnresolved(
				"localhost", 1337);
		ClientManagerCallback callback = EMPTY_CALLBACK;
		ClientManagerModel instance = createClientManagerModel(callback);
		assertNull(instance.getClient(address));
	}

	@Test
	public void testGetClientAfterRemoval() throws Exception {
		InetSocketAddress address = InetSocketAddress.createUnresolved(
				"localhost", 1337);
		final Client client = new MockClient(address);
		ClientManagerCallback callback = EMPTY_CALLBACK;
		ClientManagerModel instance = createClientManagerModel(callback);
		instance.addClient(client);
		instance.removeClient(client);
		assertNull(instance.getClient(address));
	}

	/**
	 * Test of getClients method, of class ClientManagerModel.
	 */
	@Test
	public void testGetClients() throws Exception {
		final Client firstClient = new MockClient(
				InetSocketAddress.createUnresolved("localhost", 1337));
		final Client secondClient = new MockClient(
				InetSocketAddress.createUnresolved("remotehost", 1337));
		final Client thirdClient = new MockClient(
				InetSocketAddress.createUnresolved("localhost", 1338));
		final Client fourthClient = new MockClient(
				InetSocketAddress.createUnresolved("www.example.com", 1337));
		ClientManagerCallback callback = EMPTY_CALLBACK;
		ClientManagerModel instance = createClientManagerModel(callback);
		final Set<Client> expectedState = new HashSet<>();
		assertEquals(expectedState, instance.getClients());
		addClient(firstClient, instance, expectedState);
		assertEquals(expectedState, instance.getClients());
		addClient(secondClient, instance, expectedState);
		addClient(thirdClient, instance, expectedState);
		assertEquals(expectedState, instance.getClients());
		removeClient(secondClient, instance, expectedState);
		assertEquals(expectedState, instance.getClients());
		addClient(fourthClient, instance, expectedState);
		assertEquals(expectedState, instance.getClients());
		removeClient(firstClient, instance, expectedState);
		removeClient(thirdClient, instance, expectedState);
		assertEquals(expectedState, instance.getClients());
		removeClient(fourthClient, instance, expectedState);
		assertEquals(expectedState, instance.getClients());
	}

	private static void addClient(Client client, ClientManagerModel clientManagerModel,
			Set<Client> set) throws ClientRejectedException {
		clientManagerModel.addClient(client);
		set.add(client);
	}

	private static void removeClient(Client client, ClientManagerModel clientManagerModel,
			Set<Client> set) {
		clientManagerModel.removeClient(client);
		set.remove(client);
	}

	/**
	 * Test of removeClient method, of class ClientManagerModel.
	 */
	@Test
	public void testRemoveClient() throws Exception {
		InetSocketAddress address = InetSocketAddress.createUnresolved(
				"localhost", 1337);
		final Client client = new MockClient(address);
		ClientManagerCallback callback = EMPTY_CALLBACK;
		ClientManagerModel instance = createClientManagerModel(callback);
		instance.addClient(client);
		instance.removeClient(client);
		assertFalse(instance.getClients().contains(client));
	}

	@Test
	public void testRemoveClientNotPresent() throws Exception {
		InetSocketAddress address = InetSocketAddress.createUnresolved(
				"localhost", 1337);
		final Client client = new MockClient(address);
		ClientManagerCallback callback = EMPTY_CALLBACK;
		ClientManagerModel instance = createClientManagerModel(callback);
		SpecificNotificationListener listener =
			SpecificNotificationListener.clientDisconnected(
				IsEqual.equalTo(address));
		instance.addListener(listener);
		instance.removeClient(client);
		assertFalse(listener.wasNotified());
	}

	@Test
	public void testRemoveClientListenerNotified() throws Exception {
		InetSocketAddress address = InetSocketAddress.createUnresolved(
				"localhost", 1337);
		final Client client = new MockClient(address);
		ClientManagerCallback callback = EMPTY_CALLBACK;
		ClientManagerModel instance = createClientManagerModel(callback);
		instance.addClient(client);
		SpecificNotificationListener listener =
			SpecificNotificationListener.clientDisconnected(
				IsEqual.equalTo(address));
		instance.addListener(listener);
		instance.removeClient(client);
		assertTrue(listener.wasNotified());
	}

	@Test
	public void testRemoveClientTasksCancelled() throws Exception {
		InetSocketAddress address = InetSocketAddress.createUnresolved(
				"localhost", 1337);
		TaskIdGenerator taskIdGenerator = new TaskIdGenerator();
		final MockClient client = new MockClient(address);
		for (int i = 0; i < 7; i++) {
			client.addTask(taskIdGenerator.newId());
		}
		final Set<TaskId> remainingCallbackTasks =
			new HashSet<>(client.getActiveTasks()); 
		final Set<TaskId> remainingListenerTasks =
			new HashSet<>(remainingCallbackTasks); 
		ClientManagerCallback callback = new ClientManagerCallback() {
			@Override
			public void handleTask(TaskId taskId, Task<?> task) {
			}

			@Override
			public void cancelTask(TaskId taskId) {
				assertTrue(remainingCallbackTasks.remove(taskId));
			}
		};
		ClientManagerModel instance = createClientManagerModel(callback);
		instance.addClient(client);
		for (TaskId taskId : remainingListenerTasks) {
			instance.addTask(client, taskId, new MockTask());
		}
		instance.addListener(new ClientManagerListener() {
			@Override
			public void clientConnected(InetSocketAddress address) {
				fail("Wrong listener notification");
			}

			@Override
			public void clientDisconnected(InetSocketAddress address) {
			}

			@Override
			public void taskReceived(TaskId taskId, InetSocketAddress address) {
				fail("Wrong listener notification");
			}

			@Override
			public void taskCancelled(TaskId taskId, InetSocketAddress address) {
				assertTrue(remainingListenerTasks.remove(taskId));
			}

			@Override
			public void resultSent(TaskId taskId, InetSocketAddress address) {
				fail("Wrong listener notification");
			}
		});
		instance.removeClient(client);
		assertTrue(remainingCallbackTasks.isEmpty());
		assertTrue(remainingListenerTasks.isEmpty());
	}

	/**
	 * Test of handleResult method, of class ClientManagerModel.
	 */
	@Test
	public void testHandleResult() throws Exception {
		InetSocketAddress address = InetSocketAddress.createUnresolved(
				"localhost", 1337);
		final MockClient client = new MockClient(address);
		final TaskId task = new TaskIdGenerator().newId();
		client.addTask(task);
		ClientManagerCallback callback = EMPTY_CALLBACK;
		ClientManagerModel instance = createClientManagerModel(callback);
		instance.addClient(client);
		instance.addTask(client, task, new MockTask());
		SpecificNotificationListener listener =
			SpecificNotificationListener.resultSent(IsEqual.equalTo(task),
				IsEqual.equalTo(address));
		instance.addListener(listener);
		instance.handleResult(task, new Result<Void>() {
			@Override
			public Void get() throws ExecutionException {
				return null;
			}
		});
		assertTrue(listener.wasNotified());
		assertFalse(client.getActiveTasks().contains(task));
	}

	@Test
	public void testHandleResultWithoutTask() throws Exception {
		InetSocketAddress address = InetSocketAddress.createUnresolved(
				"localhost", 1337);
		final MockClient client = new MockClient(address);
		final TaskId task = new TaskIdGenerator().newId();
		client.addTask(task);
		ClientManagerCallback callback = EMPTY_CALLBACK;
		ClientManagerModel instance = createClientManagerModel(callback);
		instance.addClient(client);
		SpecificNotificationListener listener =
			SpecificNotificationListener.anyResultSent();
		instance.addListener(listener);
		instance.handleResult(task, new Result<Void>() {
			@Override
			public Void get() throws ExecutionException {
				return null;
			}
		});
		assertFalse(listener.wasNotified());
	}

	/**
	 * Test of addTask method, of class ClientManagerModel.
	 */
	@Test
	public void testAddTask() throws Exception {
		InetSocketAddress address = InetSocketAddress.createUnresolved(
				"localhost", 1337);
		final MockClient client = new MockClient(address);
		final TaskId task = new TaskIdGenerator().newId();
		client.addTask(task);
		ClientManagerCallback callback = EMPTY_CALLBACK;
		ClientManagerModel instance = createClientManagerModel(callback);
		instance.addClient(client);
		SpecificNotificationListener listener =
			SpecificNotificationListener.taskReceived(
				IsEqual.equalTo(task), IsEqual.equalTo(address));
		instance.addListener(listener);
		instance.addTask(client, task, new MockTask());
		assertTrue(listener.wasNotified());
	}

	@Test
	public void testAddTaskAlreadyPresent() throws Exception {
		InetSocketAddress address = InetSocketAddress.createUnresolved(
				"localhost", 1337);
		final MockClient client = new MockClient(address);
		final TaskId task = new TaskIdGenerator().newId();
		client.addTask(task);
		ClientManagerCallback callback = EMPTY_CALLBACK;
		ClientManagerModel instance = createClientManagerModel(callback);
		instance.addClient(client);
		instance.addTask(client, task, new MockTask());
		SpecificNotificationListener listener =
			SpecificNotificationListener.anyTaskReceived();
		instance.addListener(listener);
		instance.addTask(client, task, new MockTask());
		assertFalse(listener.wasNotified());
	}

	@Test
	public void testAddTaskNoClient() throws Exception {
		InetSocketAddress address = InetSocketAddress.createUnresolved(
				"localhost", 1337);
		final MockClient client = new MockClient(address);
		final TaskId task = new TaskIdGenerator().newId();
		client.addTask(task);
		ClientManagerCallback callback = EMPTY_CALLBACK;
		ClientManagerModel instance = createClientManagerModel(callback);
		SpecificNotificationListener listener =
			SpecificNotificationListener.anyTaskReceived();
		instance.addListener(listener);
		instance.addTask(client, task, new MockTask());
		assertFalse(listener.wasNotified());
	}

	/**
	 * Test of cancelTask method, of class ClientManagerModel.
	 */
	@Test
	public void testCancelTask() throws Exception {
		InetSocketAddress address = InetSocketAddress.createUnresolved(
				"localhost", 1337);
		final MockClient client = new MockClient(address);
		final TaskId task = new TaskIdGenerator().newId();
		client.addTask(task);
		ClientManagerCallback callback = EMPTY_CALLBACK;
		ClientManagerModel instance = createClientManagerModel(callback);
		instance.addClient(client);
		instance.addTask(client, task, new MockTask());
		SpecificNotificationListener listener =
			SpecificNotificationListener.taskCancelled(IsEqual.equalTo(task),
				IsEqual.equalTo(address));
		instance.addListener(listener);
		instance.cancelTask(task);
		assertTrue(listener.wasNotified());
	}

	@Test
	public void testCancelTaskNotPresent() throws Exception {
		final TaskId task = new TaskIdGenerator().newId();
		ClientManagerCallback callback = EMPTY_CALLBACK;
		ClientManagerModel instance = createClientManagerModel(callback);
		SpecificNotificationListener listener =
			SpecificNotificationListener.anyTaskCancelled();
		instance.addListener(listener);
		instance.cancelTask(task);
		assertFalse(listener.wasNotified());
	}

	/**
	 * Test of addListener method, of class ClientManagerModel.
	 */
	@Test
	public void testAddListener() throws Exception {
		ClientManagerCallback callback = EMPTY_CALLBACK;
		ClientManagerModel instance = createClientManagerModel(callback);
		NotificationCountingListener firstListener =
			new NotificationCountingListener();
		NotificationCountingListener secondListener =
			new NotificationCountingListener();
		Client firstClient = new MockClient(InetSocketAddress.createUnresolved(
				"localhost", 1337));
		Client secondClient = new MockClient(InetSocketAddress.createUnresolved(
				"remotehost", 1337));
		Client thirdClient = new MockClient(InetSocketAddress.createUnresolved(
				"localhost", 1338));
		Client fourthClient = new MockClient(InetSocketAddress.createUnresolved(
				"www.example.com", 1337));
		instance.addClient(firstClient);
		instance.addListener(firstListener);
		instance.addClient(secondClient);
		instance.addListener(secondListener);
		instance.addClient(thirdClient);
		instance.addListener(firstListener);
		instance.addClient(fourthClient);
		assertEquals(3, firstListener.getNotifications());
		assertEquals(2, secondListener.getNotifications());
	}

	/**
	 * Test of removeListener method, of class ClientManagerModel.
	 */
	@Test
	public void testRemoveListener() throws Exception {
		ClientManagerCallback callback = EMPTY_CALLBACK;
		ClientManagerModel instance = createClientManagerModel(callback);
		NotificationCountingListener firstListener =
			new NotificationCountingListener();
		NotificationCountingListener secondListener =
			new NotificationCountingListener();
		Client firstClient = new MockClient(InetSocketAddress.createUnresolved(
			"localhost", 1337));
		Client secondClient = new MockClient(InetSocketAddress.createUnresolved(
			"remotehost", 1337));
		Client thirdClient = new MockClient(InetSocketAddress.createUnresolved(
			"localhost", 1338));
		Client fourthClient = new MockClient(InetSocketAddress.createUnresolved(
			"www.example.com", 1337));
		instance.addListener(firstListener);
		instance.addListener(secondListener);
		instance.addClient(firstClient);
		instance.removeListener(firstListener);
		instance.addClient(secondClient);
		instance.removeListener(firstListener);
		instance.addClient(thirdClient);
		instance.removeListener(secondListener);
		instance.addClient(fourthClient);
		assertEquals(1, firstListener.getNotifications());
		assertEquals(3, secondListener.getNotifications());
	}

	private ClientManagerModel createClientManagerModel(
			ClientManagerCallback callback) {
		return new ClientManagerModel(
				ClientManagerListenerSupport.newListenerSupport(
				new CurrentThreadExecutor()), callback);
	}
}
