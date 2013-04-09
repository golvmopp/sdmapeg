package se.sdmapeg.worker;

/**
 * Actual implementation of the Worker in the worker module.
 */
public class WorkerImpl implements Worker {
    
    Server server;

    public WorkerImpl(Server server) {
	this.server = server;
	
    }

}
