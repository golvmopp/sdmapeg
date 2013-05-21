package se.sdmapeg.server.clients.models;

import java.net.InetSocketAddress;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsAnything;
import static org.junit.Assert.*;
import se.sdmapeg.server.clients.callbacks.ClientManagerListener;
import se.sdmapeg.serverworker.TaskId;

/**
 *
 * @author niclas
 */
public final class SpecificNotificationListener implements ClientManagerListener {
	private static final Matcher<TaskId> ANY_TASK_ID =
										 new IsAnything<>();
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
	public void clientConnected(InetSocketAddress address) {
		notifiedOf(NotificationType.CLIENT_CONNECTED);
		assertThat(address, addressMatcher);
	}

	@Override
	public void clientDisconnected(InetSocketAddress address) {
		notifiedOf(NotificationType.CLIENT_DISCONNECTED);
		assertThat(address, addressMatcher);
	}

	@Override
	public void taskReceived(TaskId taskId, InetSocketAddress address) {
		notifiedOf(NotificationType.TASK_RECEIVED);
		assertThat(taskId, taskIdMatcher);
		assertThat(address, addressMatcher);
	}

	@Override
	public void taskCancelled(TaskId taskId, InetSocketAddress address) {
		notifiedOf(NotificationType.TASK_CANCELLED);
		assertThat(taskId, taskIdMatcher);
		assertThat(address, addressMatcher);
	}

	@Override
	public void resultSent(TaskId taskId, InetSocketAddress address) {
		notifiedOf(NotificationType.RESULT_SENT);
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

	public static SpecificNotificationListener anyClientConnected() {
		return new SpecificNotificationListener(
			NotificationType.CLIENT_CONNECTED, anyTaskId(), anyAddress());
	}

	public static SpecificNotificationListener clientConnected(
			Matcher<InetSocketAddress> addressMatcher) {
		return new SpecificNotificationListener(
			NotificationType.CLIENT_CONNECTED, anyTaskId(), addressMatcher);
	}

	public static SpecificNotificationListener anyClientDisconnected() {
		return new SpecificNotificationListener(
			NotificationType.CLIENT_DISCONNECTED, anyTaskId(), anyAddress());
	}

	public static SpecificNotificationListener clientDisconnected(
			Matcher<InetSocketAddress> addressMatcher) {
		return new SpecificNotificationListener(
			NotificationType.CLIENT_DISCONNECTED, anyTaskId(), addressMatcher);
	}

	public static SpecificNotificationListener anyTaskReceived() {
		return new SpecificNotificationListener(
			NotificationType.TASK_RECEIVED, anyTaskId(), anyAddress());
	}

	public static SpecificNotificationListener taskReceived(
			Matcher<TaskId> taskIdMatcher,
			Matcher<InetSocketAddress> addressMatcher) {
		return new SpecificNotificationListener(
			NotificationType.TASK_RECEIVED, taskIdMatcher, addressMatcher);
	}

	public static SpecificNotificationListener anyTaskCancelled() {
		return new SpecificNotificationListener(
			NotificationType.TASK_CANCELLED, anyTaskId(), anyAddress());
	}

	public static SpecificNotificationListener taskCancelled(
			Matcher<TaskId> taskIdMatcher,
			Matcher<InetSocketAddress> addressMatcher) {
		return new SpecificNotificationListener(
			NotificationType.TASK_CANCELLED, taskIdMatcher, addressMatcher);
	}

	public static SpecificNotificationListener anyResultSent() {
		return new SpecificNotificationListener(
			NotificationType.RESULT_SENT, anyTaskId(), anyAddress());
	}

	public static SpecificNotificationListener resultSent(
			Matcher<TaskId> taskIdMatcher,
			Matcher<InetSocketAddress> addressMatcher) {
		return new SpecificNotificationListener(
			NotificationType.RESULT_SENT, taskIdMatcher, addressMatcher);
	}

	private static Matcher<TaskId> anyTaskId() {
		return ANY_TASK_ID;
	}

	private static Matcher<InetSocketAddress> anyAddress() {
		return ANY_ADDRESS;
	}

	private enum NotificationType {
		CLIENT_CONNECTED, CLIENT_DISCONNECTED, TASK_RECEIVED, TASK_CANCELLED,
		RESULT_SENT
	}
	
}
