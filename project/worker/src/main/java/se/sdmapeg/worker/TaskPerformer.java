package se.sdmapeg.worker;

import java.util.concurrent.ExecutionException;

import se.sdmapeg.common.tasks.Task;

/**
 * Interface for classes capable of performing a particular task.
 */
public interface TaskPerformer<T extends Task<R>, R> {
	/**
	 * Performs the specified task and returns its result.
	 */
	R perform(T task) throws ExecutionException;

}
