package se.sdmapeg.worker;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import se.sdmapeg.common.tasks.Task;

public final class TaskPerformerImpl<T, R> implements TaskPerformer<T, R> {

    private ExecutorService ftp = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private BlockingDeque<Task<?>> deque;
    
    private TaskPerformerImpl(){
	deque = new LinkedBlockingDeque<>();	
    }

    @Override
    public R perform(T task) throws ExecutionException {
	
	return null;
    }
    
    public static TaskPerformerImpl newTaskPerformer(){
	return new TaskPerformerImpl();
    }

    @Override
    public void add(Task<?> task) {
	
	deque.add(task);	
    }
    
}
