package se.sdmapeg.server.clients.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;
import se.sdmapeg.common.communication.CommunicationException;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.server.clients.callbacks.ClientCallback;
import se.sdmapeg.server.test.MockConnection;
import se.sdmapeg.serverclient.communication.ClientToServerMessage;
import se.sdmapeg.serverclient.communication.ServerToClientMessage;
import se.sdmapeg.serverworker.TaskId;
/**
 *
 * @author niclas
 */
public class ClientInteractionTester {
	private final Client client;
	private final MockConnection<ServerToClientMessage, ClientToServerMessage> connection;
	private final List<ActionPerformingClientCallback> expectations = new ArrayList<>();
	private ClientCallback expected = null;
	private List<Runnable> actions = new ArrayList<>();
	private final Map<TaskId, Integer> taskIdDictionary = new HashMap<>();
	private final Map<Integer, Task<?>> taskDictionary = new HashMap<>();
	private final Set<Integer> activeTaskNumbers = new HashSet<>();
	private final AtomicInteger taskIdCounter = new AtomicInteger(0);

	public ClientInteractionTester(Client client,
			MockConnection<ServerToClientMessage, ClientToServerMessage>
				connection) {
		this.client = client;
		this.connection = connection;
	}

	public void runTest() {
		expect(null);
		TestingClientCallback testingClientCallback =
			new TestingClientCallback(new LinkedList<>(expectations));
		testingClientCallback.runTest();
		client.listen(testingClientCallback);
		testingClientCallback.verify();
	}

	public ClientInteractionTester expectTaskReceived(Task<?> task) {
		return expect(new TaskReceptionCallback(client, activeTaskNumbers,
			taskIdCounter, taskIdDictionary, taskDictionary, task));
	}

	public ClientInteractionTester expectTaskCancelled(Task<?> task) {
		return expect(new TaskCancellationCallback(client, activeTaskNumbers,
			taskIdDictionary, taskDictionary, task));
	}

	public ClientInteractionTester expectDisconnection() {
		return expect(new ConnectionClosedCallback());
	}

	private ClientInteractionTester expect(ClientCallback callback) {
		expectations.add(new ActionPerformingClientCallback(expected, actions));
		expected = callback;
		actions = new ArrayList<>();
		return this;
	}

	public ClientInteractionTester addReceived(final ClientToServerMessage message) {
		actions.add(new Runnable() {
			@Override
			public void run() {
				connection.addReceived(message);
			}
		});
		return this;
	}

	public ClientInteractionTester addReceiveException(
			final ClientToServerMessage poison,
			final CommunicationException exeption) {
		actions.add(new Runnable() {
			@Override
			public void run() {
				connection.addReceiveException(poison, exeption);
			}
		});
		return this;
	}

	public ClientInteractionTester addReceiveDisconnection() {
		actions.add(new Runnable() {
			@Override
			public void run() {
				connection.addReceiveDisconnection();
			}
		});
		return this;
	}

	public ClientInteractionTester addSendException(
			final CommunicationException exception) {
		actions.add(new Runnable() {
			@Override
			public void run() {
				connection.addSendException(exception);
			}
		});
		return this;
	}

	public ClientInteractionTester addSendDisconnection() {
		actions.add(new Runnable() {
			@Override
			public void run() {
				connection.addSendDisconnection();
			}
		});
		return this;
	}

	public ClientInteractionTester addTaskCompleted(final Task<?> task,
			final Result<?> result) {
		actions.add(new Runnable() {
			@Override
			public void run() {
				TaskId taskId = getId(task);
				client.taskCompleted(taskId, result);
				assertFalse(client.getActiveTasks().contains(taskId));
				activeTaskNumbers.remove(taskIdDictionary.get(taskId));
			}
		});
		return this;
	}

	private TaskId getId(Task<?> task) {
		return reverseLookup(taskIdDictionary, reverseLookup(taskDictionary, task));
	}

	private static <K, V> K reverseLookup(Map<K, V> map, V value) {
		for (Map.Entry<K, V> entry : map.entrySet()) {
			if (Objects.equals(value, entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}

	private static void verifyActiveTasks(Set<TaskId> activeTasks,
			Set<Integer> activeTaskNumbers,
			Map<TaskId, Integer> taskIdDictionary) {
		assertEquals(activeTaskNumbers.size(), activeTasks.size());
		for (TaskId taskId : activeTasks) {
			assertTrue(activeTaskNumbers.contains(taskIdDictionary.get(taskId)));
		}
	}

	private static final class TestingClientCallback implements ClientCallback {
		private final Queue<ActionPerformingClientCallback> delegates;

		public TestingClientCallback(Queue<ActionPerformingClientCallback> delegates) {
			this.delegates = delegates;
		}

		@Override
		public void taskReceived(TaskId taskId, Task<?> task) {
			ClientCallback delegate = delegate();
			delegate.taskReceived(taskId, task);
		}

		@Override
		public void taskCancelled(TaskId taskId) {
			ClientCallback delegate = delegate();
			delegate.taskCancelled(taskId);
		}

		@Override
		public void clientDisconnected() {
			ClientCallback delegate = delegate();
			delegate.clientDisconnected();
		}

		public void runTest() {
			delegates.poll().performActions();
		}

		public void verify() {
			assertTrue(delegates.isEmpty());
		}

		private ClientCallback delegate() {
			ClientCallback delegate = delegates.poll();
			if (delegate != null) {
				return delegate;
			}
			return new ClientCallback() {
				@Override
				public void taskReceived(TaskId taskId, Task<?> task) {
					fail("No interaction expected");
				}

				@Override
				public void taskCancelled(TaskId taskId) {
					fail("No interaction expected");
				}

				@Override
				public void clientDisconnected() {
				}
			};
		}
	}

	private static final class ActionPerformingClientCallback implements
			ClientCallback {
		private final ClientCallback delegate;
		private final Iterable<Runnable> actions;

		public ActionPerformingClientCallback(ClientCallback delegate,
			                                  Iterable<Runnable> actions) {
			this.delegate = delegate;
			this.actions = actions;
		}

		@Override
		public void taskReceived(TaskId taskId, Task<?> task) {
			delegate.taskReceived(taskId, task);
			performActions();
		}

		@Override
		public void taskCancelled(TaskId taskId) {
			delegate.taskCancelled(taskId);
			performActions();
		}

		@Override
		public void clientDisconnected() {
			delegate.clientDisconnected();
			performActions();
		}

		public void performActions() {
			for (Runnable runnable : actions) {
				runnable.run();
			}
		}
	}

	private static final class TaskReceptionCallback implements ClientCallback {
		private final Client client;
		private final Set<Integer> activeTaskNumbers;
		private final AtomicInteger taskIdCounter;
		private final Map<TaskId, Integer> taskIdDictionary;
		private final Map<Integer, Task<?>> taskDictionary;
		private final Task<?> expected;

		public TaskReceptionCallback(Client client,
				Set<Integer> activeTaskNumbers, AtomicInteger taskIdCounter,
				Map<TaskId, Integer> taskIdDictionary,
				Map<Integer, Task<?>> taskDictionary, Task<?> expected) {
			this.client = client;
			this.activeTaskNumbers = activeTaskNumbers;
			this.taskIdCounter = taskIdCounter;
			this.taskIdDictionary = taskIdDictionary;
			this.taskDictionary = taskDictionary;
			this.expected = expected;
		}

		@Override
		public void taskReceived(TaskId taskId, Task<?> task) {
			assertFalse(taskIdDictionary.containsKey(taskId));
			assertEquals(expected, task);
			Integer id = Integer.valueOf(taskIdCounter.getAndIncrement());
			activeTaskNumbers.add(id);
			taskIdDictionary.put(taskId, id);
			taskDictionary.put(id, task);
			Set<TaskId> activeTasks = client.getActiveTasks();
			verifyActiveTasks(activeTasks, activeTaskNumbers, taskIdDictionary);
		}

		@Override
		public void taskCancelled(TaskId taskId) {
			fail("Unexpected interaction");
		}

		@Override
		public void clientDisconnected() {
		}
	}

	private static final class TaskCancellationCallback implements ClientCallback {
		private final Client client;
		private final Set<Integer> activeTaskNumbers;
		private final Map<TaskId, Integer> taskIdDictionary;
		private final Map<Integer, Task<?>> taskDictionary;
		private final Task<?> expected;

		public TaskCancellationCallback(Client client,
				Set<Integer> activeTaskNumbers,
				Map<TaskId, Integer> taskIdDictionary,
				Map<Integer, Task<?>> taskDictionary,
				Task<?> expected) {
			this.client = client;
			this.activeTaskNumbers = activeTaskNumbers;
			this.taskIdDictionary = taskIdDictionary;
			this.taskDictionary = taskDictionary;
			this.expected = expected;
		}

		@Override
		public void taskReceived(TaskId taskId, Task<?> task) {
			fail("Unexpected interaction");
		}

		@Override
		public void taskCancelled(TaskId taskId) {
			Integer id = taskIdDictionary.get(taskId);
			assertEquals(expected, taskDictionary.get(id));
			activeTaskNumbers.remove(id);
			Set<TaskId> activeTasks = client.getActiveTasks();
			verifyActiveTasks(activeTasks, activeTaskNumbers, taskIdDictionary);
		}

		@Override
		public void clientDisconnected() {
		}
	}

	private static final class ConnectionClosedCallback implements ClientCallback {
		@Override
		public void taskReceived(TaskId taskId, Task<?> task) {
			fail("Unexpected interaction");
		}

		@Override
		public void taskCancelled(TaskId taskId) {
			fail("Unexpected interaction");
		}

		@Override
		public void clientDisconnected() {
		}		
	}
}
