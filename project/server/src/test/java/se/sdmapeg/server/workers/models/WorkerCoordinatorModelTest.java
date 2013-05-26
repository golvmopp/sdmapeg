package se.sdmapeg.server.workers.models;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import se.sdmapeg.common.tasks.FindNextIntTask;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.common.tasks.SimpleFailure;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.server.test.CurrentThreadExecutor;
import se.sdmapeg.server.workers.callbacks.WorkerCoordinatorCallback;
import se.sdmapeg.server.workers.callbacks.WorkerCoordinatorListener;
import se.sdmapeg.server.workers.callbacks.WorkerCoordinatorListenerSupport;
import se.sdmapeg.server.workers.exceptions.WorkerRejectedException;
import se.sdmapeg.serverworker.TaskId;
import se.sdmapeg.serverworker.TaskIdGenerator;

/**
 *
 * @author niclas
 */
public class WorkerCoordinatorModelTest {
	private static final WorkerCoordinatorCallback EMPTY_CALLBACK =
			new WorkerCoordinatorCallback() {
		@Override
		public void handleResult(TaskId taskId, Result<?> result) {
		}
	};
	public WorkerCoordinatorModelTest() {
	}

	/**
	 * Test of addWorker method, of class WorkerCoordinatorModel.
	 */
	@Test
	public void testAddWorker() throws Exception {
		Worker worker = new MockWorker(InetSocketAddress.createUnresolved(
			"localhost", 1337));
		WorkerCoordinatorCallback callback = EMPTY_CALLBACK;
		WorkerCoordinatorModel instance = createWorkerCoordinatorModel(callback);
		instance.addWorker(worker);
		Set<Worker> workers = instance.getWorkers();
		assertEquals(1, workers.size());
		assertTrue(workers.contains(worker));
	}

	@Test
	public void testAddWorkerNotifyListener() throws Exception {
		Worker worker = new MockWorker(InetSocketAddress.createUnresolved(
			"localhost", 1337));
		WorkerCoordinatorCallback callback = EMPTY_CALLBACK;
		WorkerCoordinatorModel instance = createWorkerCoordinatorModel(callback);
		SpecificNotificationListener listener =
			SpecificNotificationListener.workerConnected(
				equalTo(worker.getAddress()));
		instance.addListener(listener);
		instance.addWorker(worker);
		assertTrue(listener.wasNotified());
	}

	@Test
	public void testAddWorkerAlreadyPresent() throws Exception {
		Worker worker = new MockWorker(InetSocketAddress.createUnresolved(
			"localhost", 1337));
		WorkerCoordinatorCallback callback = EMPTY_CALLBACK;
		WorkerCoordinatorModel instance = createWorkerCoordinatorModel(callback);
		instance.addWorker(worker);
		SpecificNotificationListener listener =
			SpecificNotificationListener.anyWorkerConnected();
		instance.addListener(listener);
		try {
			instance.addWorker(worker);
			fail("Workers already present should be rejected");
		}
		catch (WorkerRejectedException exception) {
			// success
		}
		Set<Worker> workers = instance.getWorkers();
		assertEquals(1, workers.size());
		assertTrue(workers.contains(worker));
		assertFalse(listener.wasNotified());
	}

	@Test
	public void testAddWorkerDuplicate() throws Exception {
		InetSocketAddress address = InetSocketAddress.createUnresolved(
			"localhost", 1337);
		Worker firstWorker = new MockWorker(address);
		WorkerCoordinatorCallback callback = EMPTY_CALLBACK;
		WorkerCoordinatorModel instance = createWorkerCoordinatorModel(callback);
		instance.addWorker(firstWorker);
		SpecificNotificationListener listener =
			SpecificNotificationListener.anyWorkerConnected();
		instance.addListener(listener);
		Worker secondWorker = new MockWorker(address);
		try {
			instance.addWorker(secondWorker);
			fail("Duplicate workers should be rejected");
		}
		catch (WorkerRejectedException exception) {
			// success
		}
		Set<Worker> workers = instance.getWorkers();
		assertEquals(1, workers.size());
		assertTrue(workers.contains(firstWorker));
		assertFalse(listener.wasNotified());
	}

	@Test
	public void testAddWorkerAfterRemoval() throws Exception {
		InetSocketAddress address = InetSocketAddress.createUnresolved(
			"localhost", 1337);
		Worker firstWorker = new MockWorker(address);
		WorkerCoordinatorCallback callback = EMPTY_CALLBACK;
		WorkerCoordinatorModel instance = createWorkerCoordinatorModel(callback);
		instance.addWorker(firstWorker);
		instance.removeWorker(firstWorker);
		SpecificNotificationListener listener =
			SpecificNotificationListener.anyWorkerConnected();
		instance.addListener(listener);
		Worker secondWorker = new MockWorker(address);
		try {
			instance.addWorker(secondWorker);
		}
		catch (WorkerRejectedException exception) {
			fail("Adding a new worker with the same address after removing the"
					+ " old one should not fail");
		}
		Set<Worker> workers = instance.getWorkers();
		assertEquals(1, workers.size());
		assertTrue(workers.contains(secondWorker));
		assertTrue(listener.wasNotified());
	}

	/**
	 * Test of getWorker method, of class WorkerCoordinatorModel.
	 */
	@Test
	public void testGetWorker() throws Exception {
		InetSocketAddress address = InetSocketAddress.createUnresolved(
			"localhost", 1337);
		Worker worker = new MockWorker(address);
		WorkerCoordinatorCallback callback = EMPTY_CALLBACK;
		WorkerCoordinatorModel instance = createWorkerCoordinatorModel(callback);
		instance.addWorker(worker);
		assertSame(worker, instance.getWorker(address));
	}

	@Test
	public void testGetWorkerNotPresent() {
		InetSocketAddress address = InetSocketAddress.createUnresolved(
			"localhost", 1337);
		WorkerCoordinatorCallback callback = EMPTY_CALLBACK;
		WorkerCoordinatorModel instance = createWorkerCoordinatorModel(callback);
		assertNull(instance.getWorker(address));
	}

	@Test
	public void testGetWorkerAfterRemoval() throws Exception {
		InetSocketAddress address = InetSocketAddress.createUnresolved(
			"localhost", 1337);
		Worker worker = new MockWorker(address);
		WorkerCoordinatorCallback callback = EMPTY_CALLBACK;
		WorkerCoordinatorModel instance = createWorkerCoordinatorModel(callback);
		instance.addWorker(worker);
		instance.removeWorker(worker);
		assertNull(instance.getWorker(address));
	}

	/**
	 * Test of getWorkers method, of class WorkerCoordinatorModel.
	 */
	@Test
	public void testGetWorkers() throws Exception {
		Worker firstWorker = new MockWorker(
				InetSocketAddress.createUnresolved("localhost", 1337));
		Worker secondWorker = new MockWorker(
				InetSocketAddress.createUnresolved("remotehost", 1337));
		Worker thirdWorker = new MockWorker(
				InetSocketAddress.createUnresolved("localhost", 1338));
		Worker fourthWorker = new MockWorker(
				InetSocketAddress.createUnresolved("www.example.com", 1337));
		WorkerCoordinatorCallback callback = EMPTY_CALLBACK;
		WorkerCoordinatorModel instance = createWorkerCoordinatorModel(callback);
		final Set<Worker> expectedState = new HashSet<>();
		assertEquals(expectedState, instance.getWorkers());
		addWorker(firstWorker, instance, expectedState);
		assertEquals(expectedState, instance.getWorkers());
		addWorker(secondWorker, instance, expectedState);
		addWorker(thirdWorker, instance, expectedState);
		assertEquals(expectedState, instance.getWorkers());
		removeWorker(secondWorker, instance, expectedState);
		assertEquals(expectedState, instance.getWorkers());
		addWorker(fourthWorker, instance, expectedState);
		assertEquals(expectedState, instance.getWorkers());
		removeWorker(firstWorker, instance, expectedState);
		removeWorker(thirdWorker, instance, expectedState);
		assertEquals(expectedState, instance.getWorkers());
		removeWorker(fourthWorker, instance, expectedState);
		assertEquals(expectedState, instance.getWorkers());
	}

	private static void addWorker(Worker worker, WorkerCoordinatorModel workerCoordinatorModel,
			Set<Worker> set) throws WorkerRejectedException {
		workerCoordinatorModel.addWorker(worker);
		set.add(worker);
	}

	private static void removeWorker(Worker worker, WorkerCoordinatorModel workerCoordinatorModel,
			Set<Worker> set) {
		workerCoordinatorModel.removeWorker(worker);
		set.remove(worker);
	}

	/**
	 * Test of removeWorker method, of class WorkerCoordinatorModel.
	 */
	@Test
	public void testRemoveWorker() throws Exception {
		InetSocketAddress address = InetSocketAddress.createUnresolved(
			"localhost", 1337);
		Worker worker = new MockWorker(address);
		WorkerCoordinatorCallback callback = EMPTY_CALLBACK;
		WorkerCoordinatorModel instance = createWorkerCoordinatorModel(callback);
		instance.addWorker(worker);
		instance.removeWorker(worker);
		assertFalse(instance.getWorkers().contains(worker));
	}

	@Test
	public void testRemoveWorkerNotPresent() throws Exception {
		InetSocketAddress address = InetSocketAddress.createUnresolved(
			"localhost", 1337);
		Worker worker = new MockWorker(address);
		WorkerCoordinatorCallback callback = EMPTY_CALLBACK;
		WorkerCoordinatorModel instance = createWorkerCoordinatorModel(callback);
		SpecificNotificationListener listener =
			SpecificNotificationListener.workerDisonnected(equalTo(address));
		instance.addListener(listener);
		instance.removeWorker(worker);
		assertFalse(listener.wasNotified());
	}

	@Test
	public void testRemoveWorkerListenerNotified() throws Exception {
		InetSocketAddress address = InetSocketAddress.createUnresolved(
			"localhost", 1337);
		Worker worker = new MockWorker(address);
		WorkerCoordinatorCallback callback = EMPTY_CALLBACK;
		WorkerCoordinatorModel instance = createWorkerCoordinatorModel(callback);
		instance.addWorker(worker);
		SpecificNotificationListener listener =
			SpecificNotificationListener.workerDisonnected(equalTo(address));
		instance.addListener(listener);
		instance.removeWorker(worker);
		assertTrue(listener.wasNotified());
	}

	@Test
	public void testRemoveWorkerTasksReassigned() throws Exception {
		Worker firstWorker = new MockWorker(InetSocketAddress.createUnresolved(
			"localhost", 1337));
		Worker secondWorker = new MockWorker(InetSocketAddress.createUnresolved(
			"remotehost", 1337));
		TaskIdGenerator taskIdGenerator = new TaskIdGenerator();
		Set<TaskId> taskIds = generateTaskIds(10, taskIdGenerator);
		Task<?> task = FindNextIntTask.newNextIntTask(2);
		WorkerCoordinatorCallback callback = EMPTY_CALLBACK;
		WorkerCoordinatorModel instance = createWorkerCoordinatorModel(callback);
		instance.addWorker(firstWorker);
		for (TaskId taskId : taskIds) {
			instance.handleTask(taskId, task);
		}
		instance.addWorker(secondWorker);
		instance.removeWorker(firstWorker);
		assertEquals(taskIds, secondWorker.getActiveTasks());
	}

	/**
	 * Test of addListener method, of class WorkerCoordinatorModel.
	 */
	@Test
	public void testAddListener() throws Exception {
		WorkerCoordinatorCallback callback = EMPTY_CALLBACK;
		WorkerCoordinatorModel instance = createWorkerCoordinatorModel(callback);
		NotificationCountingListener firstListener =
			new NotificationCountingListener();
		NotificationCountingListener secondListener =
			new NotificationCountingListener();
		Worker firstWorker = new MockWorker(InetSocketAddress.createUnresolved(
				"localhost", 1337));
		Worker secondWorker = new MockWorker(InetSocketAddress.createUnresolved(
				"remotehost", 1337));
		Worker thirdWorker = new MockWorker(InetSocketAddress.createUnresolved(
				"localhost", 1338));
		Worker fourthWorker = new MockWorker(InetSocketAddress.createUnresolved(
				"www.example.com", 1337));
		instance.addWorker(firstWorker);
		instance.addListener(firstListener);
		instance.addWorker(secondWorker);
		instance.addListener(secondListener);
		instance.addWorker(thirdWorker);
		instance.addListener(firstListener);
		instance.addWorker(fourthWorker);
		assertEquals(3, firstListener.getNotifications());
		assertEquals(2, secondListener.getNotifications());
	}

	/**
	 * Test of removeListener method, of class WorkerCoordinatorModel.
	 */
	@Test
	public void testRemoveListener() throws Exception {
		WorkerCoordinatorCallback callback = EMPTY_CALLBACK;
		WorkerCoordinatorModel instance = createWorkerCoordinatorModel(callback);
		NotificationCountingListener firstListener =
			new NotificationCountingListener();
		NotificationCountingListener secondListener =
			new NotificationCountingListener();
		Worker firstWorker = new MockWorker(InetSocketAddress.createUnresolved(
				"localhost", 1337));
		Worker secondWorker = new MockWorker(InetSocketAddress.createUnresolved(
				"remotehost", 1337));
		Worker thirdWorker = new MockWorker(InetSocketAddress.createUnresolved(
				"localhost", 1338));
		Worker fourthWorker = new MockWorker(InetSocketAddress.createUnresolved(
				"www.example.com", 1337));
		instance.addListener(firstListener);
		instance.addListener(secondListener);
		instance.addWorker(firstWorker);
		instance.removeListener(firstListener);
		instance.addWorker(secondWorker);
		instance.removeListener(firstListener);
		instance.addWorker(thirdWorker);
		instance.removeListener(secondListener);
		instance.addWorker(fourthWorker);
		assertEquals(1, firstListener.getNotifications());
		assertEquals(3, secondListener.getNotifications());
	}

	/**
	 * Test of handleTask method, of class WorkerCoordinatorModel.
	 */
	@Test
	public void testHandleTask() throws Exception {
		InetSocketAddress address = InetSocketAddress.createUnresolved(
			"localhost", 1337);
		Worker worker = new MockWorker(address);
		TaskIdGenerator taskIdGenerator = new TaskIdGenerator();
		TaskId taskId = taskIdGenerator.newId();
		Task<?> task = FindNextIntTask.newNextIntTask(2);
		WorkerCoordinatorCallback callback = EMPTY_CALLBACK;
		WorkerCoordinatorModel instance = createWorkerCoordinatorModel(callback);
		instance.addWorker(worker);
		instance.handleTask(taskId, task);
		assertTrue(worker.getActiveTasks().contains(taskId));
	}

	@Test
	public void testHandleTaskCallbackNotified() throws Exception {
		InetSocketAddress address = InetSocketAddress.createUnresolved(
			"localhost", 1337);
		Worker worker = new MockWorker(address);
		TaskIdGenerator taskIdGenerator = new TaskIdGenerator();
		TaskId taskId = taskIdGenerator.newId();
		Task<?> task = FindNextIntTask.newNextIntTask(2);
		WorkerCoordinatorCallback callback = EMPTY_CALLBACK;
		WorkerCoordinatorModel instance = createWorkerCoordinatorModel(callback);
		instance.addWorker(worker);
		SpecificNotificationListener listener =
			SpecificNotificationListener.taskAssigned(equalTo(taskId),
				equalTo(address));
		instance.addListener(listener);
		instance.handleTask(taskId, task);
		assertTrue(listener.wasNotified());
	}

	@Test
	public void testHandleTaskNoWorkersAvailable() throws Exception {
		TaskIdGenerator taskIdGenerator = new TaskIdGenerator();
		TaskId taskId = taskIdGenerator.newId();
		Task<?> task = FindNextIntTask.newNextIntTask(2);
		NoWorkersCallback callback = new NoWorkersCallback(taskId);
		WorkerCoordinatorModel instance = createWorkerCoordinatorModel(callback);
		SpecificNotificationListener listener =
			SpecificNotificationListener.anyTaskAssigned();
		instance.addListener(listener);
		instance.handleTask(taskId, task);
		assertFalse(listener.wasNotified());
		assertTrue(callback.wasNotified());
	}

	@Test
	public void testHandleTaskNoWorkersAcceptingTasks() throws Exception {
		InetSocketAddress address = InetSocketAddress.createUnresolved(
			"localhost", 1337);
		Worker worker = new MockWorker(address);
		TaskIdGenerator taskIdGenerator = new TaskIdGenerator();
		TaskId taskId = taskIdGenerator.newId();
		Task<?> task = FindNextIntTask.newNextIntTask(2);
		NoWorkersCallback callback = new NoWorkersCallback(taskId);
		WorkerCoordinatorModel instance = createWorkerCoordinatorModel(callback);
		SpecificNotificationListener listener =
			SpecificNotificationListener.anyTaskAssigned();
		instance.addWorker(worker);
		instance.addListener(listener);
		worker.disconnect();
		instance.handleTask(taskId, task);
		assertFalse(listener.wasNotified());
		assertTrue(callback.wasNotified());
	}

	@Test
	public void testHandleTaskRejectedAssignment() throws Exception {
		Worker firstWorker = new MockWorker(
			InetSocketAddress.createUnresolved("localhost", 1337));
		MockWorker secondWorker = new MockWorker(
			InetSocketAddress.createUnresolved("remotehost", 1337));
		secondWorker.rejectTasksOnAssignment();
		TaskIdGenerator taskIdGenerator = new TaskIdGenerator();
		Set<TaskId> taskIds = generateTaskIds(
				firstWorker.getParallellWorkCapacity() + 5, taskIdGenerator);
		TaskId rejectedTaskId = taskIdGenerator.newId();
		Task<?> task = FindNextIntTask.newNextIntTask(2);
		NoWorkersCallback callback = new NoWorkersCallback(rejectedTaskId);
		WorkerCoordinatorModel instance = createWorkerCoordinatorModel(callback);
		instance.addWorker(firstWorker);
		for (TaskId taskId : taskIds) {
			instance.handleTask(taskId, task);
		}
		instance.addWorker(secondWorker);
		SpecificNotificationListener listener =
			SpecificNotificationListener.taskAssigned(
				equalTo(rejectedTaskId), equalTo(firstWorker.getAddress()));
		instance.addListener(listener);
		instance.handleTask(rejectedTaskId, task);
		assertTrue(listener.wasNotified());
		assertTrue(secondWorker.getActiveTasks().isEmpty());
		assertFalse(secondWorker.isAcceptingWork());
		assertTrue(firstWorker.getActiveTasks().contains(rejectedTaskId));
	}

	@Test
	public void testHandleTaskProperDistribution() throws Exception {
		int startPort = 1337;
		String address = "localhost";
		Set<MockWorker> availableWorkers =
			createMockWorkers(10, address, startPort);
		Set<MockWorker> unavailableWorkers =
			createMockWorkers(5, address, startPort + 10);
		Random rng = new Random(-1L);
		setRandomWorkCapacity(availableWorkers, rng);
		setRandomWorkCapacity(unavailableWorkers, rng);
		TaskIdGenerator taskIdGenerator = new TaskIdGenerator();
		Task<?> task = FindNextIntTask.newNextIntTask(2);
		WorkerCoordinatorCallback callback = EMPTY_CALLBACK;
		WorkerCoordinatorModel instance = createWorkerCoordinatorModel(callback);
		for (Worker worker : availableWorkers) {
			instance.addWorker(worker);
		}
		for (Worker worker : unavailableWorkers) {
			instance.addWorker(worker);
			worker.disconnect();
		}
		for (Worker worker : availableWorkers) {
			for (int i = 0; i < worker.getParallellWorkCapacity(); i++) {
				instance.handleTask(taskIdGenerator.newId(), task);
			}
		}
		for (Worker worker : availableWorkers) {
			assertEquals(worker.getParallellWorkCapacity(),
				worker.getActiveTasks().size());
		}
	}

	/**
	 * Test of completeTask method, of class WorkerCoordinatorModel.
	 */
	@Test
	public void testCompleteTask() throws Exception {
		int startPort = 1337;
		String address = "localhost";
		Set<MockWorker> workers = createMockWorkers(10, address, startPort);
		TaskIdGenerator taskIdGenerator = new TaskIdGenerator();
		TaskId taskId = taskIdGenerator.newId();
		Task<?> task = FindNextIntTask.newNextIntTask(2);
		Result<?> result = SimpleFailure.newSimpleFailure(
			new ExecutionException(null));
		SpecificResultCallback callback = new SpecificResultCallback(taskId,
			result);
		WorkerCoordinatorModel instance = createWorkerCoordinatorModel(callback);
		for (Worker worker : workers) {
			instance.addWorker(worker);
		}
		instance.handleTask(taskId, task);
		Worker assignedWorker = null;
		for (Worker worker : workers) {
			if (!worker.getActiveTasks().isEmpty()) {
				assignedWorker = worker;
				break;
			}
		}
		assertNotNull(assignedWorker);
		SpecificNotificationListener listener =
			SpecificNotificationListener.resultReceived(equalTo(taskId),
				equalTo(assignedWorker.getAddress()));
		instance.addListener(listener);
		instance.completeTask(taskId, result);
		assertTrue(listener.wasNotified());
		assertTrue(callback.wasNotified());
	}

	@Test
	public void testCompleteTaskNotAssigned() throws Exception {
		int startPort = 1337;
		String address = "localhost";
		Set<MockWorker> workers = createMockWorkers(10, address, startPort);
		TaskIdGenerator taskIdGenerator = new TaskIdGenerator();
		TaskId taskId = taskIdGenerator.newId();
		Result<?> result = SimpleFailure.newSimpleFailure(
			new ExecutionException(null));
		SpecificResultCallback callback = new SpecificResultCallback(taskId,
			result);
		WorkerCoordinatorModel instance = createWorkerCoordinatorModel(callback);
		for (Worker worker : workers) {
			instance.addWorker(worker);
		}
		SpecificNotificationListener listener =
			SpecificNotificationListener.anyResultReceived();
		instance.addListener(listener);
		instance.completeTask(taskId, result);
		assertFalse(listener.wasNotified());
		assertFalse(callback.wasNotified());
	}

	/**
	 * Test of cancelTask method, of class WorkerCoordinatorModel.
	 */
	@Test
	public void testCancelTask() throws Exception {
		int startPort = 1337;
		String address = "localhost";
		Set<MockWorker> workers = createMockWorkers(10, address, startPort);
		TaskIdGenerator taskIdGenerator = new TaskIdGenerator();
		TaskId taskId = taskIdGenerator.newId();
		Task<?> task = FindNextIntTask.newNextIntTask(2);
		WorkerCoordinatorCallback callback = EMPTY_CALLBACK;
		WorkerCoordinatorModel instance = createWorkerCoordinatorModel(callback);
		for (Worker worker : workers) {
			instance.addWorker(worker);
		}
		instance.handleTask(taskId, task);
		Worker assignedWorker = null;
		for (Worker worker : workers) {
			if (!worker.getActiveTasks().isEmpty()) {
				assignedWorker = worker;
				break;
			}
		}
		assertNotNull(assignedWorker);
		SpecificNotificationListener listener =
			SpecificNotificationListener.anyTaskAborted(equalTo(taskId),
				equalTo(assignedWorker.getAddress()));
		instance.addListener(listener);
		instance.cancelTask(taskId);
		assertTrue(listener.wasNotified());
		assertFalse(assignedWorker.getActiveTasks().contains(taskId));
	}

	@Test
	public void testCancelTaskNotAssigned() throws Exception {
		int startPort = 1337;
		String address = "localhost";
		Set<MockWorker> workers = createMockWorkers(10, address, startPort);
		TaskIdGenerator taskIdGenerator = new TaskIdGenerator();
		TaskId taskId = taskIdGenerator.newId();
		WorkerCoordinatorCallback callback = EMPTY_CALLBACK;
		WorkerCoordinatorModel instance = createWorkerCoordinatorModel(callback);
		for (Worker worker : workers) {
			instance.addWorker(worker);
		}
		SpecificNotificationListener listener =
			SpecificNotificationListener.anyTaskAborted();
		instance.addListener(listener);
		instance.cancelTask(taskId);
		assertFalse(listener.wasNotified());
	}

	/**
	 * Test of stealTasks method, of class WorkerCoordinatorModel.
	 */
	@Test
	public void testStealTasks() throws Exception {
		InetSocketAddress address = InetSocketAddress.createUnresolved(
			"localhost", 1337);
		MockWorker worker = new MockWorker(address);
		TaskIdGenerator taskIdGenerator = new TaskIdGenerator();
		Set<TaskId> taskIds = generateTaskIds(
				worker.getParallellWorkCapacity() + 21, taskIdGenerator);
		Task<?> task = FindNextIntTask.newNextIntTask(2);
		WorkerCoordinatorCallback callback = EMPTY_CALLBACK;
		WorkerCoordinatorModel instance = createWorkerCoordinatorModel(callback);
		instance.addWorker(worker);
		for (TaskId taskId : taskIds) {
			instance.handleTask(taskId, task);
		}
		instance.stealTasks(10);
		assertEquals(10, worker.getTaskStealingRequests().get(0).intValue());
	}

	@Test
	public void testStealTasksDistribution() throws Exception {
		MockWorker firstWorker =
			new MockWorker(InetSocketAddress.createUnresolved(
				"localhost", 1337));
		MockWorker secondWorker =
			new MockWorker(InetSocketAddress.createUnresolved(
				"remotehost", 1337));
		TaskIdGenerator taskIdGenerator = new TaskIdGenerator();
		Set<TaskId> taskIds = generateTaskIds(
				firstWorker.getParallellWorkCapacity()
				+ secondWorker.getParallellWorkCapacity() + 16,
				taskIdGenerator);
		Task<?> task = FindNextIntTask.newNextIntTask(2);
		WorkerCoordinatorCallback callback = EMPTY_CALLBACK;
		WorkerCoordinatorModel instance = createWorkerCoordinatorModel(callback);
		instance.addWorker(firstWorker);
		instance.addWorker(secondWorker);
		for (TaskId taskId : taskIds) {
			instance.handleTask(taskId, task);
		}
		instance.stealTasks(10);
		int firstWorkerTheft = firstWorker.getTaskStealingRequests().get(0);
		int secondWorkerTheft = secondWorker.getTaskStealingRequests().get(0);
		assertTrue(firstWorkerTheft < 10);
		assertTrue(secondWorkerTheft < 10);
		assertEquals(10, firstWorkerTheft + secondWorkerTheft);
	}

	@Test
	public void testStealTasksFewerThanDesired() throws Exception {
		InetSocketAddress address = InetSocketAddress.createUnresolved(
			"localhost", 1337);
		MockWorker worker = new MockWorker(address);
		TaskIdGenerator taskIdGenerator = new TaskIdGenerator();
		Set<TaskId> taskIds = generateTaskIds(
				worker.getParallellWorkCapacity() + 5, taskIdGenerator);
		Task<?> task = FindNextIntTask.newNextIntTask(2);
		WorkerCoordinatorCallback callback = EMPTY_CALLBACK;
		WorkerCoordinatorModel instance = createWorkerCoordinatorModel(callback);
		instance.addWorker(worker);
		for (TaskId taskId : taskIds) {
			instance.handleTask(taskId, task);
		}
		instance.stealTasks(10);
		int workerTheft = worker.getTaskStealingRequests().get(0);
		assertTrue(workerTheft <= worker.getActiveTasks().size()
						- worker.getParallellWorkCapacity());
	}

	@Test
	public void testStealTasksNoTasksAvailable() throws Exception {
		InetSocketAddress address = InetSocketAddress.createUnresolved(
			"localhost", 1337);
		MockWorker worker = new MockWorker(address);
		WorkerCoordinatorCallback callback = EMPTY_CALLBACK;
		WorkerCoordinatorModel instance = createWorkerCoordinatorModel(callback);
		instance.addWorker(worker);
		instance.stealTasks(10);
		assertTrue(worker.getTaskStealingRequests().isEmpty());
	}

	/**
	 * Test of reassignTask method, of class WorkerCoordinatorModel.
	 */
	@Test
	public void testReassignTask() throws Exception {
		MockWorker firstWorker =
			new MockWorker(InetSocketAddress.createUnresolved(
				"localhost", 1337));
		MockWorker secondWorker =
			new MockWorker(InetSocketAddress.createUnresolved(
				"remotehost", 1337));
		TaskIdGenerator taskIdGenerator = new TaskIdGenerator();
		TaskId reassignedTaskId = taskIdGenerator.newId();
		Set<TaskId> taskIds = generateTaskIds(
				firstWorker.getParallellWorkCapacity() + 10, taskIdGenerator);
		Task<?> task = FindNextIntTask.newNextIntTask(2);
		WorkerCoordinatorCallback callback = EMPTY_CALLBACK;
		WorkerCoordinatorModel instance = createWorkerCoordinatorModel(callback);
		instance.addWorker(firstWorker);
		instance.handleTask(reassignedTaskId, task);
		for (TaskId taskId : taskIds) {
			instance.handleTask(taskId, task);
		}
		instance.addWorker(secondWorker);
		instance.reassignTask(reassignedTaskId);
		assertEquals(taskIds, firstWorker.getActiveTasks());
		assertTrue(secondWorker.getActiveTasks().contains(reassignedTaskId));
	}

	@Test
	public void testReassignTaskNotAssigned() throws Exception {
		MockWorker firstWorker =
			new MockWorker(InetSocketAddress.createUnresolved(
				"localhost", 1337));
		MockWorker secondWorker =
			new MockWorker(InetSocketAddress.createUnresolved(
				"remotehost", 1337));
		TaskIdGenerator taskIdGenerator = new TaskIdGenerator();
		TaskId unassignedTaskId = taskIdGenerator.newId();
		Set<TaskId> taskIds = generateTaskIds(
				firstWorker.getParallellWorkCapacity() + 10, taskIdGenerator);
		Task<?> task = FindNextIntTask.newNextIntTask(2);
		WorkerCoordinatorCallback callback = EMPTY_CALLBACK;
		WorkerCoordinatorModel instance = createWorkerCoordinatorModel(callback);
		instance.addWorker(firstWorker);
		for (TaskId taskId : taskIds) {
			instance.handleTask(taskId, task);
		}
		instance.addWorker(secondWorker);
		instance.reassignTask(unassignedTaskId);
		assertEquals(taskIds, firstWorker.getActiveTasks());
		assertFalse(secondWorker.getActiveTasks().contains(unassignedTaskId));
	}

	private static WorkerCoordinatorModel createWorkerCoordinatorModel(
			WorkerCoordinatorCallback callback) {
		return new WorkerCoordinatorModel(
				WorkerCoordinatorListenerSupport.newListenerSupport(
					new CurrentThreadExecutor()), callback);
	}

	private static Set<TaskId> generateTaskIds(int n,
			TaskIdGenerator taskIdGenerator) {
		Set<TaskId> taskIds = new HashSet<>();
		for (int i = 0; i < n; i++) {
			taskIds.add(taskIdGenerator.newId());
		}
		return taskIds;
	}

	private static Set<MockWorker> createMockWorkers(int n, String address,
			int startPort) {
		Set<MockWorker> workers = new HashSet<>();
		for (int i = 0; i < n; i++) {
			workers.add(new MockWorker(InetSocketAddress.createUnresolved(
				address, startPort + i)));
		}
		return workers;
	}

	private void setRandomWorkCapacity(Set<MockWorker> availableWorkers,
									   Random rng) {
		for (MockWorker mockWorker : availableWorkers) {
			mockWorker.setParallellWorkCapacity(0x01 << rng.nextInt(6));
		}
	}

	private static final class NoWorkersCallback
			implements WorkerCoordinatorCallback {
		private final TaskId expectedTaskId;
		private boolean notified = false;

		public NoWorkersCallback(TaskId expectedTaskId) {
			this.expectedTaskId = expectedTaskId;
		}

		public boolean wasNotified() {
			return notified;
		}

		@Override
		public void handleResult(TaskId taskId, Result<?> result) {
			notified = true;
			assertEquals(expectedTaskId, taskId);
			try {
				result.get();
				fail("Expected exception to be thrown");
			} catch (ExecutionException ex) {
				// success
			}
		}		
	}

	private static final class SpecificResultCallback
			implements WorkerCoordinatorCallback {
		private final TaskId expectedTaskId;
		private final Result<?> expectedResult;
		private boolean notified = false;

		public SpecificResultCallback(TaskId expectedTaskId,
				Result<?> expectedResult) {
			this.expectedTaskId = expectedTaskId;
			this.expectedResult = expectedResult;
		}

		public boolean wasNotified() {
			return notified;
		}

		@Override
		public void handleResult(TaskId taskId, Result<?> result) {
			notified = true;
			assertEquals(expectedTaskId, taskId);
			assertEquals(expectedResult, result);
		}
	}
}
