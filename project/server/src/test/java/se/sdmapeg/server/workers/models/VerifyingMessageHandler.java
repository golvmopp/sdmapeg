package se.sdmapeg.server.workers.models;

import static org.hamcrest.CoreMatchers.*;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsAnything;
import static org.junit.Assert.*;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.serverworker.TaskId;
import se.sdmapeg.serverworker.communication.ServerToWorkerMessage;
import se.sdmapeg.serverworker.communication.TaskCancellationMessage;
import se.sdmapeg.serverworker.communication.TaskMessage;
import se.sdmapeg.serverworker.communication.WorkStealingRequestMessage;

/**
 *
 * @author niclas
 */
public final class VerifyingMessageHandler implements ServerToWorkerMessage.Handler<Void> {
	private final Matcher<? super TaskMessage> taskMessageMatcher;
	private final Matcher<? super TaskCancellationMessage> taskCancellationMatcher;
	private final Matcher<? super WorkStealingRequestMessage> workStealingMatcher;
	private boolean called = false;

	private VerifyingMessageHandler(Matcher<? super TaskMessage> taskMessageMatcher,
									Matcher<? super TaskCancellationMessage> taskCancellationMatcher,
									Matcher<? super WorkStealingRequestMessage> workStealingMatcher) {
		this.taskMessageMatcher = taskMessageMatcher;
		this.taskCancellationMatcher = taskCancellationMatcher;
		this.workStealingMatcher = workStealingMatcher;
	}

	public boolean wasCalled() {
		return called;
	}

	@Override
	public Void handle(TaskMessage message) {
		assertThat(message, taskMessageMatcher);
		called = true;
		return null;
	}

	@Override
	public Void handle(TaskCancellationMessage message) {
		assertThat(message, taskCancellationMatcher);
		called = true;
		return null;
	}

	@Override
	public Void handle(WorkStealingRequestMessage message) {
		assertThat(message, workStealingMatcher);
		called = true;
		return null;
	}

	public static VerifyingMessageHandler anyTaskMessage() {
		return new VerifyingMessageHandler(anything(), nothing(), nothing());
	}

	public static VerifyingMessageHandler taskMessage(
			Matcher<? super TaskId> taskIdMatcher,
			Matcher<? super Task<?>> taskMatcher) {
		Matcher<TaskMessage> matcher = both(
				new FeatureMatcher<TaskMessage, TaskId>(taskIdMatcher,
					"task id", "task id") {
			@Override
			protected TaskId featureValueOf(TaskMessage actual) {
				return actual.getTaskId();
			}
		}).and(new FeatureMatcher<TaskMessage, Task<?>>(taskMatcher, "task",
				"task") {
			@Override
			protected Task<?> featureValueOf(TaskMessage actual) {
				return actual.getTask();
			}
		});
		return new VerifyingMessageHandler(matcher, nothing(), nothing());
	}

	public static VerifyingMessageHandler anyTaskCancellation() {
		return new VerifyingMessageHandler(nothing(), anything(), nothing());
	}

	public static VerifyingMessageHandler taskCancellation(
			Matcher<? super TaskId> taskIdMatcher) {
		Matcher<TaskCancellationMessage> matcher =
			new FeatureMatcher<TaskCancellationMessage, TaskId>(taskIdMatcher,
				"task id", "task id") {
			@Override
			protected TaskId featureValueOf(TaskCancellationMessage actual) {
				return actual.getTaskId();
			}
		};
		return new VerifyingMessageHandler(nothing(), matcher, nothing());
	}

	public static VerifyingMessageHandler anyWorkStealing() {
		return new VerifyingMessageHandler(nothing(), nothing(), anything());
	}

	public static VerifyingMessageHandler workStealing(
			Matcher<? super Integer> desiredMatcher) {
		Matcher<WorkStealingRequestMessage> matcher =
			new FeatureMatcher<WorkStealingRequestMessage, Integer>(
				desiredMatcher, "task id", "task id") {
			@Override
			protected Integer featureValueOf(WorkStealingRequestMessage actual) {
				return Integer.valueOf(actual.getDesired());
			}
		};
		return new VerifyingMessageHandler(nothing(), nothing(), matcher);
	}

	private static <T> Matcher<T> nothing() {
		return not(new IsAnything<T>());
	}

	private static <T> Matcher<T> anything() {
		return new IsAnything<>();
	}
	
}
