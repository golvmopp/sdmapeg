package se.sdmapeg.common.tasks;

/**
 * Interface for the use of the visitor pattern with tasks.
 *
 */
public interface TaskPerformer {
    public Result<String> performPythonTask(PythonTask pythonCode);
}
