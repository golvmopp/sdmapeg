package se.sdmapeg.server;

import se.sdmapeg.common.Result;
import se.sdmapeg.serverworker.TaskId;

/**
 * Callback interface for Worker Coordinators. 
 */
public interface WorkerCoordinatorCallback {
   /**
    * Handles the result of the task represented by the TaskId.
    * @param taskId Unique id paired to a task
    * @param result Result to task represented by the TaskId
    */
    void handleResult(TaskId taskId, Result<?> result);


}
