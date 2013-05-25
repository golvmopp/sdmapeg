package se.sdmapeg.server.workers.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import static org.junit.Assert.*;
import se.sdmapeg.common.communication.CommunicationException;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.server.test.MockConnection;
import se.sdmapeg.server.workers.callbacks.WorkerCallback;
import se.sdmapeg.server.workers.exceptions.TaskRejectedException;
import se.sdmapeg.serverworker.TaskId;
import se.sdmapeg.serverworker.communication.ServerToWorkerMessage;
import se.sdmapeg.serverworker.communication.WorkerToServerMessage;
/**
 *
 * @author niclas
 */
public class WorkerInteractionTester {
	private final Worker worker;
	private final MockConnection<ServerToWorkerMessage, WorkerToServerMessage> connection;
	private final List<ActionPerformingWorkerCallback> expectations = new ArrayList<>();
	private WorkerCallback expected = null;
	private List<Runnable> actions = new ArrayList<>();
	private final Set<TaskId> activeTasks = new HashSet<>();

	public WorkerInteractionTester(Worker worker,
			MockConnection<ServerToWorkerMessage, WorkerToServerMessage> connection) {
		this.worker = worker;
		this.connection = connection;
	}

	public void runTest() {
		expect(null);
		TestingWorkerCallback testingWorkerCallback =
			new TestingWorkerCallback(new LinkedList<>(expectations));
		testingWorkerCallback.runTest();
		worker.listen(testingWorkerCallback);
		testingWorkerCallback.verify();
	}

	public WorkerInteractionTester expectTaskCompleted(TaskId taskId,
			Result<?> result) {
		return expect(new TaskCompletionCallback(worker, taskId, result,
			activeTasks));
	}

	public WorkerInteractionTester expectTaskTheft(TaskId taskId) {
		return expect(new TaskTheftCallback(worker, taskId, activeTasks));
	}

	public WorkerInteractionTester expectWorkRequest() {
		return expect(new WorkRequestCallback());
	}

	public WorkerInteractionTester expectDisconnection() {
		return expect(new ConnectionClosedCallback());
	}

	private WorkerInteractionTester expect(WorkerCallback callback) {
		expectations.add(new ActionPerformingWorkerCallback(expected, actions));
		expected = callback;
		actions = new ArrayList<>();
		return this;
	}

	public WorkerInteractionTester addReceived(final WorkerToServerMessage message) {
		actions.add(new Runnable() {
			@Override
			public void run() {
				connection.addReceived(message);
			}
		});
		return this;
	}

	public WorkerInteractionTester addReceiveException(
			final WorkerToServerMessage poison,
			final CommunicationException exeption) {
		actions.add(new Runnable() {
			@Override
			public void run() {
				connection.addReceiveException(poison, exeption);
			}
		});
		return this;
	}

	public WorkerInteractionTester addReceiveDisconnection() {
		actions.add(new Runnable() {
			@Override
			public void run() {
				connection.addReceiveDisconnection();
			}
		});
		return this;
	}

	public WorkerInteractionTester addSendException(
			final CommunicationException exception) {
		actions.add(new Runnable() {
			@Override
			public void run() {
				connection.addSendException(exception);
			}
		});
		return this;
	}

	public WorkerInteractionTester addSendDisconnection() {
		actions.add(new Runnable() {
			@Override
			public void run() {
				connection.addSendDisconnection();
			}
		});
		return this;
	}

	public WorkerInteractionTester addSuccessfulTaskAssignment(
			final TaskId taskId, final Task<?> task) {
		actions.add(new Runnable() {
			@Override
			public void run() {
				try {
					worker.assignTask(taskId, task);
					activeTasks.add(taskId);
				} catch (TaskRejectedException ex) {
					fail("Task rejection not expected");
				}
			}
		});
		return this;
	}

	public WorkerInteractionTester addRejectedTaskAssignment(
			final TaskId taskId, final Task<?> task) {
		actions.add(new Runnable() {
			@Override
			public void run() {
				try {
					worker.assignTask(taskId, task);
					fail("Task rejection expected");
				} catch (TaskRejectedException ex) {
					// success
				}
			}
		});
		return this;
	}

	public WorkerInteractionTester addTaskCancellation(final TaskId taskId) {
		actions.add(new Runnable() {
			@Override
			public void run() {
				worker.cancelTask(taskId);
				activeTasks.remove(taskId);
			}
		});
		return this;
	}

	public WorkerInteractionTester addServerDisconnection() {
		actions.add(new Runnable() {
			@Override
			public void run() {
				worker.disconnect();
			}
		});
		return this;
	}

	private static final class TestingWorkerCallback implements WorkerCallback {
		private final Queue<ActionPerformingWorkerCallback> delegates;

		public TestingWorkerCallback(Queue<ActionPerformingWorkerCallback> delegates) {
			this.delegates = delegates;
		}

		@Override
		public void taskCompleted(TaskId taskId, Result<?> result) {
			delegate().taskCompleted(taskId, result);
		}

		@Override
		public void taskStolen(TaskId taskId) {
			delegate().taskStolen(taskId);
		}

		@Override
		public void workerDisconnected() {
			delegate().workerDisconnected();
		}

		@Override
		public void workRequested() {
			delegate().workRequested();
		}

		public void runTest() {
			delegates.poll().performActions();
		}

		public void verify() {
			assertTrue(delegates.isEmpty());
		}

		private WorkerCallback delegate() {
			WorkerCallback delegate = delegates.poll();
			if (delegate != null) {
				return delegate;
			}
			return new WorkerCallback() {
				@Override
				public void taskCompleted(TaskId taskId, Result<?> result) {
					fail("No interaction expected");
				}

				@Override
				public void taskStolen(TaskId taskId) {
					fail("No interaction expected");
				}

				@Override
				public void workerDisconnected() {
				}

				@Override
				public void workRequested() {
					fail("No interaction expected");
				}
			};
		}
	}

	private static final class ActionPerformingWorkerCallback implements
			WorkerCallback {
		private final WorkerCallback delegate;
		private final Iterable<Runnable> actions;

		public ActionPerformingWorkerCallback(WorkerCallback delegate,
			                                  Iterable<Runnable> actions) {
			this.delegate = delegate;
			this.actions = actions;
		}

		public void performActions() {
			for (Runnable runnable : actions) {
				runnable.run();
			}
		}

		@Override
		public void taskCompleted(TaskId taskId, Result<?> result) {
			delegate.taskCompleted(taskId, result);
			performActions();
		}

		@Override
		public void taskStolen(TaskId taskId) {
			delegate.taskStolen(taskId);
			performActions();
		}

		@Override
		public void workerDisconnected() {
			delegate.workerDisconnected();
			performActions();
		}

		@Override
		public void workRequested() {
			delegate.workRequested();
			performActions();
		}
	}

	private static final class TaskTheftCallback implements WorkerCallback {
		private final Worker worker;
		private final TaskId expectedTaskId;
		private final Set<TaskId> activeTasks;

		public TaskTheftCallback(Worker worker, TaskId expectedTaskId,
				Set<TaskId> activeTasks) {
			this.worker = worker;
			this.expectedTaskId = expectedTaskId;
			this.activeTasks = activeTasks;
		}

		@Override
		public void taskCompleted(TaskId taskId, Result<?> result) {
			fail("Unexpected interaction");
		}

		@Override
		public void taskStolen(TaskId taskId) {
			assertEquals(expectedTaskId, taskId);
			assertFalse(worker.getActiveTasks().contains(taskId));
			activeTasks.remove(taskId);
			assertEquals(activeTasks, worker.getActiveTasks());
		}

		@Override
		public void workerDisconnected() {
			fail("Unexpected interaction");
		}

		@Override
		public void workRequested() {
			fail("Unexpected interaction");
		}
	}

	private static final class TaskCompletionCallback implements WorkerCallback {
		private final Worker worker;
		private final TaskId expectedTaskId;
		private final Result<?> expectedResult;
		private final Set<TaskId> activeTasks;

		public TaskCompletionCallback(Worker worker, TaskId expectedTaskId,
				Result<?> expectedResult, Set<TaskId> activeTasks) {
			this.worker = worker;
			this.expectedTaskId = expectedTaskId;
			this.expectedResult = expectedResult;
			this.activeTasks = activeTasks;
		}

		@Override
		public void taskCompleted(TaskId taskId, Result<?> result) {
			assertEquals(expectedTaskId, taskId);
			assertEquals(expectedResult, result);
			activeTasks.remove(taskId);
			assertEquals(activeTasks, worker.getActiveTasks());
		}

		@Override
		public void taskStolen(TaskId taskId) {
			fail("Unexpected interaction");
		}

		@Override
		public void workerDisconnected() {
			fail("Unexpected interaction");
		}

		@Override
		public void workRequested() {
			fail("Unexpected interaction");
		}
	}

	private static final class ConnectionClosedCallback implements WorkerCallback {
		@Override
		public void taskCompleted(TaskId taskId, Result<?> result) {
			fail("Unexpected interaction");
		}

		@Override
		public void taskStolen(TaskId taskId) {
			fail("Unexpected interaction");
		}

		@Override
		public void workerDisconnected() {
		}

		@Override
		public void workRequested() {
			fail("Unexpected interaction");
		}
	}

	private static final class WorkRequestCallback implements WorkerCallback {
		@Override
		public void taskCompleted(TaskId taskId, Result<?> result) {
			fail("Unexpected interaction");
		}

		@Override
		public void taskStolen(TaskId taskId) {
			fail("Unexpected interaction");
		}

		@Override
		public void workerDisconnected() {
			fail("Unexpected interaction");
		}

		@Override
		public void workRequested() {
		}
	}
}
