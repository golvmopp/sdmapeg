package se.sdmapeg.server.workers.models;

import java.net.InetSocketAddress;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsAnything;
import static org.junit.Assert.*;
import se.sdmapeg.server.workers.callbacks.WorkerCoordinatorListener;
import se.sdmapeg.serverworker.TaskId;

/**
 *
 * @author niclas
 */
public final class SpecificNotificationListener implements WorkerCoordinatorListener {
	private static final Matcher<TaskId> ANY_TASK_ID = new IsAnything<>();
	private static final Matcher<InetSocketAddress> ANY_ADDRESS =
		new IsAnything<>();
	private final NotificationType expected;
	private final Matcher<TaskId> taskIdMatcher;
	private final Matcher<InetSocketAddress> addressMatcher;
	private boolean notified = false;

	private SpecificNotificationListener(NotificationType expected,
										Matcher<TaskId> taskIdMatcher,
										Matcher<InetSocketAddress> addressMatcher) {
		this.expected = expected;
		this.taskIdMatcher = taskIdMatcher;
		this.addressMatcher = addressMatcher;
	}

	@Override
	public void workerConnected(InetSocketAddress address) {
		notifiedOf(NotificationType.WORKER_CONNECTED);
		assertThat(address, addressMatcher);
	}

	@Override
	public void workerDisconnected(InetSocketAddress address) {
		notifiedOf(NotificationType.WORKER_DISCONNECTED);
		assertThat(address, addressMatcher);
	}

	@Override
	public void resultReceived(TaskId taskId, InetSocketAddress address) {
		notifiedOf(NotificationType.RESULT_RECEIVED);
		assertThat(taskId, taskIdMatcher);
		assertThat(address, addressMatcher);
	}

	@Override
	public void taskAssigned(TaskId taskId, InetSocketAddress address) {
		notifiedOf(NotificationType.TASK_ASSIGNED);
		assertThat(taskId, taskIdMatcher);
		assertThat(address, addressMatcher);
	}

	@Override
	public void taskAborted(TaskId taskId, InetSocketAddress address) {
		notifiedOf(NotificationType.TASK_ABORTED);
		assertThat(taskId, taskIdMatcher);
		assertThat(address, addressMatcher);
	}

	public boolean wasNotified() {
		return notified;
	}

	private void notifiedOf(NotificationType notificationType) {
		assertEquals(expected, notificationType);
		notified = true;
	}

	public static SpecificNotificationListener anyWorkerConnected() {
		return new SpecificNotificationListener(
				NotificationType.WORKER_CONNECTED, anyTaskId(), anyAddress());
	}

	public static SpecificNotificationListener workerConnected(
			Matcher<InetSocketAddress> addressMatcher) {
		return new SpecificNotificationListener(
				NotificationType.WORKER_CONNECTED, anyTaskId(), addressMatcher);
	}

	public static SpecificNotificationListener anyWorkerDisonnected() {
		return new SpecificNotificationListener(
				NotificationType.WORKER_DISCONNECTED, anyTaskId(), anyAddress());
	}

	public static SpecificNotificationListener workerDisonnected(
			Matcher<InetSocketAddress> addressMatcher) {
		return new SpecificNotificationListener(
				NotificationType.WORKER_DISCONNECTED, anyTaskId(),
					addressMatcher);
	}

	public static SpecificNotificationListener anyResultReceived() {
		return new SpecificNotificationListener(
				NotificationType.RESULT_RECEIVED, anyTaskId(), anyAddress());
	}

	public static SpecificNotificationListener resultReceived(
			Matcher<TaskId> taskIdMatcher,
			Matcher<InetSocketAddress> addressMatcher) {
		return new SpecificNotificationListener(
				NotificationType.RESULT_RECEIVED, taskIdMatcher,
					addressMatcher);
	}

	public static SpecificNotificationListener anyTaskAssigned() {
		return new SpecificNotificationListener(
				NotificationType.TASK_ASSIGNED, anyTaskId(), anyAddress());
	}

	public static SpecificNotificationListener taskAssigned(
			Matcher<TaskId> taskIdMatcher,
			Matcher<InetSocketAddress> addressMatcher) {
		return new SpecificNotificationListener(
				NotificationType.TASK_ASSIGNED, taskIdMatcher, addressMatcher);
	}

	public static SpecificNotificationListener anyTaskAborted() {
		return new SpecificNotificationListener(
				NotificationType.TASK_ABORTED, anyTaskId(), anyAddress());
	}

	public static SpecificNotificationListener anyTaskAborted(
			Matcher<TaskId> taskIdMatcher,
			Matcher<InetSocketAddress> addressMatcher) {
		return new SpecificNotificationListener(
				NotificationType.TASK_ABORTED, taskIdMatcher, addressMatcher);
	}

	private static Matcher<TaskId> anyTaskId() {
		return ANY_TASK_ID;
	}

	private static Matcher<InetSocketAddress> anyAddress() {
		return ANY_ADDRESS;
	}

	private enum NotificationType {
		WORKER_CONNECTED, WORKER_DISCONNECTED, TASK_ASSIGNED, TASK_ABORTED,
		RESULT_RECEIVED;
	}
}
