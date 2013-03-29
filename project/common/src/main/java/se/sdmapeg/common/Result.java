package se.sdmapeg.common;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;

/**
 * Interface representing the result of a task. The result may be either
 * success or failure.
 */
public interface Result<R> extends Serializable {
	/**
	 * Returns the result if the task was performed successfully, or throws an
	 * exception if an error occurred while performing the task.
	 * @return the result of the task.
	 * @throws ExecutionException if performing the task failed for some reason.
	 */
	R get() throws ExecutionException;

}
