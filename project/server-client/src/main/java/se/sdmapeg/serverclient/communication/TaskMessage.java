package se.sdmapeg.serverclient.communication;

import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.serverclient.ClientTaskId;

/**
 * Implementation of ServerToWorkerMessage containing 
 *  the task to be sent to the worker. 
 *
 */

public interface TaskMessage extends ClientToServerMessage{
    @Override
    <T> T accept(Handler<T> handler);
    
    /**
     * Returns the Task of this TaskMessage.
     * @return the Task of this TaskMessage
     */
    Task<?> getTask();

    /**
     * Returns the TaskId of this TaskMessage.
     * @return the TaskId of this TaskMessage
     */
    ClientTaskId getTaskId();
}
