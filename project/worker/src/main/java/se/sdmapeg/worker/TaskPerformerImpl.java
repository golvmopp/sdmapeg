package se.sdmapeg.worker;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import se.sdmapeg.common.tasks.Task;

public final class TaskPerformerImpl implements TaskPerformer<T, R> {

    ExecutorService ftp = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    BlockingDeque deque;
    
    public TaskPerformerImpl(){
	
    }


    @Override
    public R perform(T task) throws ExecutionException {
	// TODO Auto-generated method stub
	return null;
    }
    
    public void add(Task task){
	
    }
    
}
