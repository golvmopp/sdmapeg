package se.sdmapeg.serverworker;

import se.sdmapeg.common.Id;

/**
 * Representation for the Server's Task ID.
 */
public final class TaskId implements Id {
	private final long id;

	private TaskId(long id) {
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
		if (!(obj instanceof TaskId)) {
			return false;
		}
		TaskId other = (TaskId) obj;
		if (id != other.id) {
			return false;
		}
		return true;
	}

	/**
	 * Returns an ID matching the specified underlying long id.
	 */
	public static TaskId getId(long id) {
		return new TaskId(id);
	}
}
