package se.sdmapeg.worker;

import se.sdmapeg.common.tasks.PythonTask;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.common.tasks.TaskPerformer;

/**
 * An implementation of a task performer.
 */
public final class TaskPerformerImpl implements TaskPerformer {

    @Override
    public Result<String> performPythonTask(PythonTask pythonCode) {
	return null;
    }
}