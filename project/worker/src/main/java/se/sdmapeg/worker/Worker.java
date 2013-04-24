package se.sdmapeg.worker;

import se.sdmapeg.common.listeners.Listenable;

/**
 * Interface for representing the actual worker.
 */
public interface Worker extends Listenable<WorkerListener> {
   
    void start();
    
    void stop();
}
