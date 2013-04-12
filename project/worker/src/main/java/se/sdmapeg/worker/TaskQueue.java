package se.sdmapeg.worker;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import se.sdmapeg.common.tasks.Task;

public class TaskQueue {

    private BlockingDeque<Task> deque;
    private TaskPerformer taskPerformer;
    
    private TaskQueue(TaskPerformer taskPerformer){
	this.taskPerformer = taskPerformer;
	deque = new LinkedBlockingDeque<Task>();
    }
    
    public void addToQueue(Task task){
	deque.add(task);
    }
    
    public static TaskQueue newTaskQueue(TaskPerformer taskPerformer){
	return new TaskQueue(taskPerformer);
    }
}
