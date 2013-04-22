package se.sdmapeg.worker;

import se.sdmapeg.common.tasks.*;
import se.sdmapeg.worker.taskperformers.FindNextInteger;
import se.sdmapeg.worker.taskperformers.PythonTaskPerformer;

import java.util.concurrent.ExecutionException;

/**
 * An implementation of a task performer.
 */
public final class TaskPerformerImpl implements TaskPerformer {

    @Override
    public Result<String> performPythonTask(PythonTask pythonCode) {
	    try {
		    return new SimpleResult<>(PythonTaskPerformer.execute(pythonCode.getPythonCode()));
	    } catch (ExecutionException ex) {
		    return new SimpleFailure<>(ex);
	    }
    }

	@Override
	public Result<Integer> performFindNextIntTask(FindNextIntTask nextIntTask) {
		try {
			return new SimpleResult<>(Integer.valueOf(FindNextInteger.findNextInteger(nextIntTask.getStart())));
		} catch (ExecutionException ex) {
			return new SimpleFailure<>(ex);
		}
	}
}