package se.sdmapeg.server.workers.models;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import static org.hamcrest.CoreMatchers.*;
import org.hamcrest.Matcher;
import static org.junit.Assert.*;
import org.junit.Test;
import se.sdmapeg.common.communication.CommunicationException;
import se.sdmapeg.common.communication.Connection;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.common.tasks.SimpleFailure;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.server.test.MockConnection;
import se.sdmapeg.server.test.MockTask;
import se.sdmapeg.server.test.PairIterator;
import se.sdmapeg.server.workers.exceptions.TaskRejectedException;
import se.sdmapeg.serverworker.TaskId;
import se.sdmapeg.serverworker.TaskIdGenerator;
import se.sdmapeg.serverworker.communication.ServerToWorkerMessage;
import se.sdmapeg.serverworker.communication.WorkerToServerMessage;
import se.sdmapeg.serverworker.communication.WorkerToServerMessage.Handler;
import se.sdmapeg.serverworker.communication.WorkerToServerMessageFactory;

/**
 *
 * @author niclas
 */
public class WorkerImplTest {
	private static final WorkerToServerMessage POISON_MESSAGE =
											   createPoisonMessage();
	private static final InetSocketAddress DUMMY_ADDRESS =
		InetSocketAddress.createUnresolved("localhost", 1337);

	public WorkerImplTest() {
	}

	/**
	 * Test of getAddress method, of class WorkerImpl.
	 */
	@Test
	public void testGetAddress() {
		List<InetSocketAddress> addresses = Arrays.asList(
				new InetSocketAddress[]{
					InetSocketAddress.createUnresolved("localhost", 1337),
					InetSocketAddress.createUnresolved("remotehost", 1337),
					InetSocketAddress.createUnresolved("someotherhost", 1337),
					InetSocketAddress.createUnresolved("192.168.0.1", 1337),
					InetSocketAddress.createUnresolved("127.0.0.1", 1337),
					InetSocketAddress.createUnresolved("www.example.com", 1337)});
		List<MockConnection<ServerToWorkerMessage, WorkerToServerMessage>> connections =
			createConnections(addresses);
		List<Worker> workers = createWorkers(connections);
		for (PairIterator.Pair<InetSocketAddress, Worker> clientAddressPair :
				PairIterator.iterable(addresses, workers)) {
			InetSocketAddress address = clientAddressPair.getLeft();
			Worker worker = clientAddressPair.getRight();
			assertEquals(address, worker.getAddress());
		}
	}

	/**
	 * Test of assignTask method, of class WorkerImpl.
	 */
	@Test
	public void testAssignTask() throws Exception {
		MockConnection<ServerToWorkerMessage, WorkerToServerMessage> mockConnection =
			mockConnection(DUMMY_ADDRESS);
		TaskIdGenerator taskIdGenerator = new TaskIdGenerator();
		final Task<?> task = new MockTask();
		final TaskId taskId = taskIdGenerator.newId();
		Worker instance = WorkerImpl.newWorker(mockConnection);
		instance.assignTask(taskId, task);
		assertTrue(instance.getActiveTasks().contains(taskId));
		ServerToWorkerMessage sent = mockConnection.getSent();
		assertNotNull(sent);
		VerifyingMessageHandler handler = VerifyingMessageHandler.taskMessage(
				equalTo(taskId), equalTask(task));
		sent.accept(handler);
		assertTrue(handler.wasCalled());
	}

	@Test
	public void testAssignTaskConnectionClosed() throws Exception {
		MockConnection<ServerToWorkerMessage, WorkerToServerMessage> mockConnection =
			mockConnection(DUMMY_ADDRESS);
		TaskIdGenerator taskIdGenerator = new TaskIdGenerator();
		final Task<?> task = new MockTask();
		final TaskId taskId = taskIdGenerator.newId();
		Worker instance = WorkerImpl.newWorker(mockConnection);
		mockConnection.addSendDisconnection();
		instance.assignTask(taskId, task);
		assertTrue(instance.getActiveTasks().contains(taskId));
		ServerToWorkerMessage sent = mockConnection.getSent();
		assertNull(sent);
	}

	@Test
	public void testAssignTaskFailure() throws Exception {
		MockConnection<ServerToWorkerMessage, WorkerToServerMessage> mockConnection =
			mockConnection(DUMMY_ADDRESS);
		TaskIdGenerator taskIdGenerator = new TaskIdGenerator();
		final Task<?> task = new MockTask();
		final TaskId taskId = taskIdGenerator.newId();
		Worker instance = WorkerImpl.newWorker(mockConnection);
		new WorkerInteractionTester(instance, mockConnection)
				.addReceiveDisconnection()
				.expectDisconnection()
				.runTest();
		try {
			instance.assignTask(taskId, task);
			fail("Assigning a task to a disconnected worker should throw an exception");
		} catch (TaskRejectedException ex) {
			// success
		}
		assertFalse(instance.getActiveTasks().contains(taskId));
		ServerToWorkerMessage sent = mockConnection.getSent();
		assertNull(sent);
	}

	/**
	 * Test of cancelTask method, of class WorkerImpl.
	 */
	@Test
	public void testCancelTask() throws Exception {
		MockConnection<ServerToWorkerMessage, WorkerToServerMessage> mockConnection =
			mockConnection(DUMMY_ADDRESS);
		TaskIdGenerator taskIdGenerator = new TaskIdGenerator();
		TaskId taskId = taskIdGenerator.newId();
		Set<TaskId> otherTaskIds = generateTaskIds(taskIdGenerator, 5);
		Task<?> task = new MockTask();
		Worker instance = WorkerImpl.newWorker(mockConnection);
		instance.assignTask(taskId, task);
		mockConnection.getSent().accept(VerifyingMessageHandler.anyTaskMessage());
		for (TaskId otherTaskId : otherTaskIds) {
			instance.assignTask(otherTaskId, task);
			mockConnection.getSent().accept(
					VerifyingMessageHandler.anyTaskMessage());
		}
		instance.cancelTask(taskId);
		assertEquals(otherTaskIds, instance.getActiveTasks());
		ServerToWorkerMessage sent = mockConnection.getSent();
		VerifyingMessageHandler handler =
			VerifyingMessageHandler.taskCancellation(equalTo(taskId));
		sent.accept(handler);
		assertTrue(handler.wasCalled());
	}

	@Test
	public void testCancelTaskNotPresent() throws Exception {
		MockConnection<ServerToWorkerMessage, WorkerToServerMessage> mockConnection =
			mockConnection(DUMMY_ADDRESS);
		TaskIdGenerator taskIdGenerator = new TaskIdGenerator();
		TaskId taskId = taskIdGenerator.newId();
		Set<TaskId> otherTaskIds = generateTaskIds(taskIdGenerator, 5);
		Task<?> task = new MockTask();
		Worker instance = WorkerImpl.newWorker(mockConnection);
		for (TaskId otherTaskId : otherTaskIds) {
			instance.assignTask(otherTaskId, task);
			mockConnection.getSent().accept(
					VerifyingMessageHandler.anyTaskMessage());
		}
		instance.cancelTask(taskId);
		assertFalse(instance.getActiveTasks().contains(taskId));
		ServerToWorkerMessage sent = mockConnection.getSent();
		assertNull(sent);
	}

	/**
	 * Test of stealTasks method, of class WorkerImpl.
	 */
	@Test
	public void testStealTasks() throws Exception {
		MockConnection<ServerToWorkerMessage, WorkerToServerMessage> mockConnection =
			mockConnection(DUMMY_ADDRESS);
		Worker instance = WorkerImpl.newWorker(mockConnection);
		instance.stealTasks(10);
		ServerToWorkerMessage sent = mockConnection.getSent();
		VerifyingMessageHandler handler =
			VerifyingMessageHandler.workStealing(equalTo(Integer.valueOf(10)));
		sent.accept(handler);
		assertTrue(handler.wasCalled());
	}

	/**
	 * Test of getActiveTasks method, of class WorkerImpl.
	 */
	@Test
	public void testGetActiveTasks() throws Exception {
		MockConnection<ServerToWorkerMessage, WorkerToServerMessage> mockConnection =
			mockConnection(DUMMY_ADDRESS);
		TaskIdGenerator taskIdGenerator = new TaskIdGenerator();
		Worker instance = WorkerImpl.newWorker(mockConnection);
		TaskId firsTaskId = taskIdGenerator.newId();
		TaskId secondTaskId = taskIdGenerator.newId();
		TaskId thirdTaskId = taskIdGenerator.newId();
		Task<?> task = new MockTask();
		Set<TaskId> tasksShadow = new HashSet<>();
		assertEquals(tasksShadow, instance.getActiveTasks());
		assignShadowedTask(instance, firsTaskId, task, tasksShadow);
		assertEquals(tasksShadow, instance.getActiveTasks());
		assignShadowedTask(instance, secondTaskId, task, tasksShadow);
		assertEquals(tasksShadow, instance.getActiveTasks());
		cancelShadowedTask(instance, firsTaskId, tasksShadow);
		assertEquals(tasksShadow, instance.getActiveTasks());
		assignShadowedTask(instance, thirdTaskId, task, tasksShadow);
		assertEquals(tasksShadow, instance.getActiveTasks());
		cancelShadowedTask(instance, thirdTaskId, tasksShadow);
		assertEquals(tasksShadow, instance.getActiveTasks());
		cancelShadowedTask(instance, secondTaskId, tasksShadow);
		assertEquals(tasksShadow, instance.getActiveTasks());
		TaskId fourthTaskId = taskIdGenerator.newId();
		Result<?> result = SimpleFailure.newSimpleFailure(
				new ExecutionException(null));
		TaskId fifthTaskId = taskIdGenerator.newId();
		TaskId sixthTaskId = taskIdGenerator.newId();
		new WorkerInteractionTester(instance, mockConnection)
				.addSuccessfulTaskAssignment(fourthTaskId, task)
				.addSuccessfulTaskAssignment(fifthTaskId, task)
				.addReceived(WorkerToServerMessageFactory.newResultMessage(
					fourthTaskId, result))
				.expectTaskCompleted(fourthTaskId, result)
				.addReceived(WorkerToServerMessageFactory.newResultMessage(
					fifthTaskId, result))
				.addSuccessfulTaskAssignment(sixthTaskId, task)
				.addReceived(WorkerToServerMessageFactory.newResultMessage(
					sixthTaskId, result))
				.expectTaskCompleted(fifthTaskId, result)
				.expectTaskCompleted(sixthTaskId, result)
				.expectWorkRequest()
				.addReceiveDisconnection()
				.expectDisconnection()
				.runTest();
	}

	@Test
	public void testGetActiveTasksReturnsSnapshot() throws Exception {
		MockConnection<ServerToWorkerMessage, WorkerToServerMessage> mockConnection =
			mockConnection(DUMMY_ADDRESS);
		TaskIdGenerator taskIdGenerator = new TaskIdGenerator();
		Worker instance = WorkerImpl.newWorker(mockConnection);
		TaskId taskId = taskIdGenerator.newId();
		Task<?> task = new MockTask();
		Set<TaskId> first = instance.getActiveTasks();
		instance.assignTask(taskId, task);
		Set<TaskId> second = instance.getActiveTasks();
		assertNotEquals(first, second);
	}

	/**
	 * Test of getLoad method, of class WorkerImpl.
	 */
	@Test
	public void testGetLoad() throws Exception {
		MockConnection<ServerToWorkerMessage, WorkerToServerMessage> mockConnection =
			mockConnection(DUMMY_ADDRESS);
		TaskIdGenerator taskIdGenerator = new TaskIdGenerator();
		Worker instance = WorkerImpl.newWorker(mockConnection);
		int expectedLoad = - instance.getParallellWorkCapacity();
		assertEquals(expectedLoad, instance.getLoad());
		Task<?> task = new MockTask();
		for (TaskId taskId : generateTaskIds(taskIdGenerator, 200)) {
			expectedLoad++;
			instance.assignTask(taskId, task);
			assertEquals(expectedLoad, instance.getLoad());
		}
	}

	/**
	 * Test of isAcceptingWork method, of class WorkerImpl.
	 */
	@Test
	public void testIsAcceptingWork() {
		MockConnection<ServerToWorkerMessage, WorkerToServerMessage> mockConnection =
			mockConnection(DUMMY_ADDRESS);
		Worker instance = WorkerImpl.newWorker(mockConnection);
		assertTrue(instance.isAcceptingWork());
		instance.disconnect();
		new WorkerInteractionTester(instance, mockConnection)
				.expectDisconnection()
				.runTest();
		assertFalse(instance.isAcceptingWork());
	}

	/**
	 * Test of disconnect method, of class WorkerImpl.
	 */
	@Test
	public void testDisconnectFailure() {
		MockConnection<ServerToWorkerMessage, WorkerToServerMessage> mockConnection =
			mockConnection(DUMMY_ADDRESS);
		Worker instance = WorkerImpl.newWorker(mockConnection);
		assertTrue(instance.isAcceptingWork());
		mockConnection.setExceptionOnClose(true);
		instance.disconnect();
		new WorkerInteractionTester(instance, mockConnection)
				.expectDisconnection()
				.runTest();
		assertFalse(instance.isAcceptingWork());
	}

	/**
	 * Test of getParallellWorkCapacity method, of class WorkerImpl.
	 */
	@Test
	public void testGetParallellWorkCapacity() {
		MockConnection<ServerToWorkerMessage, WorkerToServerMessage> mockConnection =
			mockConnection(DUMMY_ADDRESS);
		Worker instance = WorkerImpl.newWorker(mockConnection);
		assertEquals(1, instance.getParallellWorkCapacity());
		new WorkerInteractionTester(instance, mockConnection)
				.addReceived(WorkerToServerMessageFactory
					.newWorkerIdentificationMessage(20))
				.expectWorkRequest()
				.addReceiveDisconnection()
				.expectDisconnection()
				.runTest();
		assertEquals(20, instance.getParallellWorkCapacity());
	}

	/**
	 * Test of listen method, of class WorkerImpl.
	 */
	@Test
	public void testListen() {
		MockConnection<ServerToWorkerMessage, WorkerToServerMessage> mockConnection =
			mockConnection(DUMMY_ADDRESS);
		TaskIdGenerator taskIdGenerator = new TaskIdGenerator();
		Task<?> task = new MockTask();
		Worker instance = WorkerImpl.newWorker(mockConnection);
		TaskId firstId = taskIdGenerator.newId();
		TaskId secondId = taskIdGenerator.newId();
		Set<TaskId> queue = generateTaskIds(taskIdGenerator, 20);
		WorkerInteractionTester tester =
			new WorkerInteractionTester(instance, mockConnection)
				.addReceived(WorkerToServerMessageFactory
					.newWorkerIdentificationMessage(8))
				.expectWorkRequest()
				.addSuccessfulTaskAssignment(firstId, task)
				.addSuccessfulTaskAssignment(secondId, task)
				.addSuccessfulTaskAssignment(taskIdGenerator.newId(), task)
				.addSuccessfulTaskAssignment(taskIdGenerator.newId(), task)
				.addSuccessfulTaskAssignment(taskIdGenerator.newId(), task);
		for (TaskId taskId : queue) {
			tester.addSuccessfulTaskAssignment(taskId, task);
		}
		tester.addReceived(WorkerToServerMessageFactory
					.newWorkStealingResponseMessage(queue));
		for (TaskId taskId : queue) {
			tester.expectTaskTheft(taskId);
		}
		Result<?> result = SimpleFailure.newSimpleFailure(
				new ExecutionException(null));
		tester.addTaskCancellation(firstId)
				.addReceived(WorkerToServerMessageFactory.newResultMessage(
					secondId, result))
				.expectTaskCompleted(secondId, result)
				.expectWorkRequest()
				.addReceiveDisconnection()
				.expectDisconnection()
				.runTest();
		new WorkerInteractionTester(instance, mockConnection)
				.expectDisconnection()
				.runTest();
	}

	@Test
	public void testListenSendDisconnection() {
		MockConnection<ServerToWorkerMessage, WorkerToServerMessage> mockConnection =
			mockConnection(DUMMY_ADDRESS);
		mockConnection.addSendDisconnection();
		TaskIdGenerator taskIdGenerator = new TaskIdGenerator();
		Worker instance = WorkerImpl.newWorker(mockConnection);
		new WorkerInteractionTester(instance, mockConnection)
				.addSuccessfulTaskAssignment(taskIdGenerator.newId(),
					new MockTask())
				.expectDisconnection()
				.runTest();
	}

	@Test
	public void testListenSendException() {
		MockConnection<ServerToWorkerMessage, WorkerToServerMessage> mockConnection =
			mockConnection(DUMMY_ADDRESS);
		mockConnection.addSendException(new CommunicationException());
		TaskIdGenerator taskIdGenerator = new TaskIdGenerator();
		Worker instance = WorkerImpl.newWorker(mockConnection);
		new WorkerInteractionTester(instance, mockConnection)
				.addSuccessfulTaskAssignment(taskIdGenerator.newId(),
					new MockTask())
				.expectDisconnection()
				.runTest();
	}

	@Test
	public void testListenReceiveException() {
		MockConnection<ServerToWorkerMessage, WorkerToServerMessage> mockConnection =
			mockConnection(DUMMY_ADDRESS);
		mockConnection.addReceiveException(createPoisonMessage(),
			new CommunicationException());
		Worker instance = WorkerImpl.newWorker(mockConnection);
		new WorkerInteractionTester(instance, mockConnection)
				.expectDisconnection()
				.runTest();
	}

	@Test
	public void testListenServerDisconnection() {
		MockConnection<ServerToWorkerMessage, WorkerToServerMessage> mockConnection =
			mockConnection(DUMMY_ADDRESS);
		Worker instance = WorkerImpl.newWorker(mockConnection);
		new WorkerInteractionTester(instance, mockConnection)
				.addReceived(WorkerToServerMessageFactory
					.newWorkerIdentificationMessage(8))
				.expectWorkRequest()
				.addServerDisconnection()
				.expectDisconnection()
				.runTest();
	}

	/**
	 * Test of toString method, of class WorkerImpl.
	 */
	@Test
	public void testToString() {
		List<InetSocketAddress> addresses = Arrays.asList(
				new InetSocketAddress[]{
					InetSocketAddress.createUnresolved("localhost", 1337),
					InetSocketAddress.createUnresolved("remotehost", 1337),
					InetSocketAddress.createUnresolved("someotherhost", 1337),
					InetSocketAddress.createUnresolved("192.168.0.1", 1337),
					InetSocketAddress.createUnresolved("127.0.0.1", 1337),
					InetSocketAddress.createUnresolved("www.example.com", 1337)});
		List<MockConnection<ServerToWorkerMessage, WorkerToServerMessage>> connections =
			createConnections(addresses);
		List<Worker> workers = createWorkers(connections);
		for (PairIterator.Pair<InetSocketAddress, Worker> clientAddressPair :
				PairIterator.iterable(addresses, workers)) {
			InetSocketAddress address = clientAddressPair.getLeft();
			Worker worker = clientAddressPair.getRight();
			assertThat(worker.toString(), containsString(address.toString()));
		}
	}

	private static MockConnection<ServerToWorkerMessage, WorkerToServerMessage> mockConnection(
			InetSocketAddress address) {
		return new MockConnection<>(address, POISON_MESSAGE);
	}

	private static WorkerToServerMessage createPoisonMessage() {
		return new WorkerToServerMessage() {
			@Override
			public <T> T accept(Handler<T> handler) {
				throw new UnsupportedOperationException();
			}
		};
	}

	private List<MockConnection<ServerToWorkerMessage, WorkerToServerMessage>> createConnections(
			List<InetSocketAddress> addresses) {
		List<MockConnection<ServerToWorkerMessage, WorkerToServerMessage>> connections =
																		   new ArrayList<>(addresses
				.size());
		for (InetSocketAddress address : addresses) {
			connections.add(mockConnection(address));
		}
		return Collections.unmodifiableList(connections);
	}

	private List<Worker> createWorkers(
			List<? extends Connection<ServerToWorkerMessage, WorkerToServerMessage>> connections) {
		List<Worker> workers = new ArrayList<>(connections.size());
		for (Connection<ServerToWorkerMessage, WorkerToServerMessage> mockConnection :
			 connections) {
			workers.add(WorkerImpl.newWorker(mockConnection));
		}
		return workers;
	}

	@SuppressWarnings("unchecked")
	private static Matcher<Task<?>> equalTask(final Task<?> task) {
		return (Matcher<Task<?>>) equalTo(task);
	}

	private static Set<TaskId> generateTaskIds(TaskIdGenerator generator, int n) {
		Set<TaskId> taskIds = new HashSet<>();
		for (int i = 0; i < n; i++) {
			taskIds.add(generator.newId());
		}
		return taskIds;
	}

	private void assignShadowedTask(Worker instance, TaskId taskId,
			Task<?> task, Set<TaskId> taskShadow) throws TaskRejectedException {
		instance.assignTask(taskId, task);
		taskShadow.add(taskId);
	}

	private void cancelShadowedTask(Worker instance, TaskId taskId,
			Set<TaskId> tasksShadow) {
		instance.cancelTask(taskId);
		tasksShadow.remove(taskId);
	}
}
