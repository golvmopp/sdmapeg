package se.sdmapeg.worker;

/**
 * Interface for representing the actual worker.
 */
public interface Worker {
   
    void start();
    
    void stop();

	void addListener(WorkerListener listener);

	void removeListener(WorkerListener listener);
}
