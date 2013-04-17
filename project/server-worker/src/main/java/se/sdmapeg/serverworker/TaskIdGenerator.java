package se.sdmapeg.serverworker;

import se.sdmapeg.common.Id;
import se.sdmapeg.common.IdGenerator;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementation of IdGenerator for TaskIds.
 */
public class TaskIdGenerator implements IdGenerator {
	private AtomicLong idCount = new AtomicLong(0L);

	@Override
	public Id newId() {
		return TaskIdImpl.getId(idCount.getAndIncrement());
	}

	private final static class TaskIdImpl implements TaskId {
		private final long id;

		private TaskIdImpl(long id) {
			this.id = id;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (id ^ (id >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof TaskIdImpl)) {
				return false;
			}
			TaskIdImpl other = (TaskIdImpl) obj;
			if (id != other.id) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return "TaskId{" + id + '}';
		}

		/**
		 * Returns an ID matching the specified underlying long id.
		 */
		public static TaskId getId(long id) {
			return new TaskIdImpl(id);
		}
	}
}
