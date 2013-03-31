package se.sdmapeg.server;

import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.serverworker.TaskId;

/**
 * Callback interface for Client Managers.
 */
public interface ClientManagerCallback {
    
	/**
	 * Handles the task.
	 * @param taskId Unique id paired to a task
	 * @param task Task to be handled
	 */
	void handleTask(TaskId taskId, Task<?> task);
}
