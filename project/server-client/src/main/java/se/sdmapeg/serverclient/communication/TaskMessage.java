package se.sdmapeg.serverclient.communication;

import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.serverclient.ClientTaskId;

/**
 * Implementation of ServerToWorkerMessage containing 
 *  the task to be sent to the worker. 
 *
 */

public final class TaskMessage implements ClientToServerMessage{
    
   
    private static final long serialVersionUID = -5680777161207194946L;
    private final Task<?> task;
    private final ClientTaskId taskID;
    
    private TaskMessage(Task<?> task, ClientTaskId taskID){
	this.task = task;
	this.taskID = taskID;
    }
    
    @Override
    public <T> T accept(Handler<T> handler) {
	return handler.handle(this);
    }
    
    /**
     * Returns the Task of this TaskMessage.
     * @return the Task of this TaskMessage
     */
    public Task<?> getTask(){
	return task;
    }

    /**
     * Returns the TaskId of this TaskMessage.
     * @return the TaskId of this TaskMessage
     */
    public ClientTaskId getTaskId(){
	return taskID;
    }
    
    /**
     * Returns a new TaskMessage.
     * 
     * @param task The task to be sent to the worker
     * @param taskID The TaskId linked with the Task
     * @return the new ResultMessage
     */
    public static ClientToServerMessage newTaskMessage(Task<?> task,
	    ClientTaskId taskID){
	return new TaskMessage(task, taskID);
    }  
}
