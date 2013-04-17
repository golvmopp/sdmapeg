package se.sdmapeg.worker.taskperformers;

import java.util.concurrent.ExecutionException;
import se.sdmapeg.common.tasks.Task;

/**
 * Interface for classes capable of performing a particular task.
 */
public interface SpecificTaskPerformer<T extends Task<R>, R> {
	/**
	 * Performs the specified task and returns its result.
	 */
	R perform(T task) throws ExecutionException;

}
