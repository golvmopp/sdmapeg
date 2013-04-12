package se.sdmapeg.worker;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;

import se.sdmapeg.common.tasks.Task;

/**
 * An implementation of a task performer.
 */
public final class TaskPerformerImpl<T, R> implements TaskPerformer<T, R> {

    @Override
    public R perform(T task) throws ExecutionException {
	// TODO Auto-generated method stub
	return null;
    }
    
/*
    private ThreadFactory threadFactory;
    private ExecutorService ftp;
    
    private TaskPerformerImpl(){
	threadFactory = new ThreadFactory() {
	    
	    @Override
	    public Thread newThread(Runnable r) {
		return null;
	    }
	};
	ftp = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),threadFactory);
    }

    @Override
    public R perform(T task) throws ExecutionException {
	return null;
    }
    
    public static TaskPerformerImpl newTaskPerformer(){
	return new TaskPerformerImpl();
    }
  */  
}
