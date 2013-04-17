package se.sdmapeg.worker;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingDeque;

import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.serverworker.TaskId;

public class TaskQueue {

    private BlockingDeque<FutureTask> deque;
    private ExecutorService workerThreadpool;
    
    private TaskQueue(ExecutorService workerThreadPool){
	this.workerThreadpool = workerThreadPool;
	this.deque = new LinkedBlockingDeque<FutureTask>();
    }
    
    public void addToQueue(FutureTask task){
	deque.add(task);
	workerThreadpool.submit(task);
    }
    
    public FutureTask stealTask(TaskId taskId){
	
    }
    
    
    public static TaskQueue newTaskQueue(ExecutorService threadPool){
	return new TaskQueue(threadPool);
    }
}
