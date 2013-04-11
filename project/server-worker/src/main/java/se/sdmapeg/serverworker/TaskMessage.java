package se.sdmapeg.serverworker;

import se.sdmapeg.common.tasks.Task;

/**
 * Implementation of ServerToWorkerMessage containing 
 *  the task to be sent to the worker. 
 *
 */

public final class TaskMessage implements ServerToWorkerMessage{
    
   
    private static final long serialVersionUID = 368380099586489028L;
    private final Task<?> task;
    private final TaskId taskID;
    
    private TaskMessage(Task<?> task, TaskId taskID){
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
    public TaskId getTaskId(){
	return taskID;
    }
    
    /**
     * Returns a new TaskMessage.
     * 
     * @param task The task to be sent to the worker
     * @param taskID The TaskId linked with the Task
     * @return the new ResultMessage
     */
    public static ServerToWorkerMessage newTaskMessage(Task<?> task,
	    TaskId taskID){
	return new TaskMessage(task, taskID);
    }  
}
