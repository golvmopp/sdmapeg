package se.sdmapeg.common.tasks;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;


/**
 * Interface for representing a task.
 *
 * @param <R> the type of the result expected by this task.
 */
public interface Task<R> extends Serializable {
	/**
	 * Uses the specified TaskPerformer to perform this task and returns its
	 * result.
	 *
	 * @param taskPerformer the TaskPerformer to perform this task.
	 * @return the result of the task.
	 */
	Result<R> perform(TaskPerformer taskPerformer);

	/**
	 * Returns the type of result expected by this task.
	 *
	 * @return the type of result expected by this task.
	 */
	Class<R> resultType();
}
