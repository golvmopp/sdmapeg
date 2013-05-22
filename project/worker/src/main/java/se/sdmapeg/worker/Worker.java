package se.sdmapeg.worker;

import se.sdmapeg.common.listeners.Listenable;
import se.sdmapeg.serverworker.TaskId;

/**
 * Interface for representing the actual worker.
 */
public interface Worker extends Listenable<WorkerListener> {
   
    void start();
    
    void stop();

	String getHost();
	
	String getTaskName(TaskId taskId);
}
