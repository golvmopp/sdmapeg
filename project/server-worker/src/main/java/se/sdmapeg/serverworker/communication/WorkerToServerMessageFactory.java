package se.sdmapeg.serverworker.communication;

import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.serverworker.TaskId;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class WorkerToServerMessageFactory {

	/**
	 * Returns a WorkStealingResponseMessage.
	 * @return a WorkStealingResponseMessage
	 */
	public static WorkStealingResponseMessage newWorkStealingResponseMessage(Set<TaskId> stolenIds) {
		return new WorkStealingResponseMessageImpl(stolenIds);
	}

	/**
	 * Creates a new worker identification message with the specified data.
	 *
	 * @param parallelWorkCapacity	the number of tasks that the worker
	 *								identifying itself is capable of
	 *								performing in parallel
	 * @return the created message
	 */
	public static WorkerToServerMessage newWorkerIdentificationMessage(
			int parallelWorkCapacity) {
		return new WorkerIdentificationMessageImpl(parallelWorkCapacity);
	}

	/**
	 * Returns a new ResultMessage.
	 * @param id The ClientTaskId linked with the result
	 * @param result The Result to be sent back to the client
	 * @return the new ResultMessage
	 */
	public static WorkerToServerMessage newResultMessage(TaskId id, Result<?> result) {
		return new ResultMessageImpl(id, result);
	}

	private static class WorkStealingResponseMessageImpl implements WorkStealingResponseMessage {
		private static final long serialVersionUID = 1594015099229454379L;
		private final Set<TaskId> stolenIds;

		private WorkStealingResponseMessageImpl(Set<TaskId> stolenIds) {
			this.stolenIds = new HashSet<>(stolenIds);
		}

		@Override
		public <T> T accept(Handler<T> handler) {
			return handler.handle(this);
		}

		public Set<TaskId> getStolenTasks() {
			return Collections.unmodifiableSet(stolenIds);
		}
	}

	private static class WorkerIdentificationMessageImpl implements WorkerIdentificationMessage {
		private static final long serialVersionUID = -387238449857548L;
		private final int parallelWorkCapacity;

		private WorkerIdentificationMessageImpl(int parallelWorkCapacity) {
			this.parallelWorkCapacity = parallelWorkCapacity;
		}

		@Override
		public <T> T accept(Handler<T> handler) {
			return handler.handle(this);
		}

		/**
		 * Returns the number of tasks that the worker identifying itself is capable
		 * of performing in parallel.
		 *
		 * @return	the number of tasks that the worker identifying itself is
		 *			capable of performing in parallel
		 */
		public int getParallelWorkCapacity() {
			return parallelWorkCapacity;
		}
	}

	private static class ResultMessageImpl implements ResultMessage {
		private static final long serialVersionUID = 1L;
		private final TaskId id;
		private final Result<?> result;

		private ResultMessageImpl(TaskId id, Result<?> result) {
			this.id = id;
			this.result = result;
		}

		@Override
		public <T> T accept(Handler<T> handler) {
			return handler.handle(this);
		}

		/**
		 * Returns the ClientTaskId of this ResultMessage.
		 * @return the ClientTaskId of this ResultMessage
		 */
		public TaskId getId() {
			return id;
		}

		/**
		 * Returns the Result of this ResultMessage.
		 * @return the Result of this ResultMessage
		 */
		public Result<?> getResult() {
			return result;
		}
	}
}
