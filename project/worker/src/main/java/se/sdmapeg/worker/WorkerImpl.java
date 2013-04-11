package se.sdmapeg.worker;

import org.junit.runner.Result;

import se.sdmapeg.common.communication.CommunicationException;
import se.sdmapeg.common.communication.ConnectionClosedException;
import se.sdmapeg.common.communication.Message;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.serverworker.TaskMessage;

/**
 * Actual implementation of the Worker in the worker module.
 */
public class WorkerImpl implements Worker {
    
    Server server;
    TaskPerformer taskPerformer;

    public WorkerImpl(Server server) {
	this.server = server;
	this.taskPerformer =  TaskPerformerImpl.newTaskPerformer();
	new TaskMessageListener(server).run();
	
	
    }
    
    private final class TaskMessageListener implements Runnable {
        
        Server server;
        
        private TaskMessageListener(Server server) {
    	
            this.server = server;
        }
    
            @Override
            public void run() {
        	while(true) {
        	    try {
        		Message message = server.receive();
        		if (message instanceof TaskMessage) {
        		    TaskMessage taskMessage = (TaskMessage) message;
        		    taskPerformer.add(((TaskMessage) message).getTask());  
        		}
        	    } catch (ConnectionClosedException ex) {
        		break;
        	    } catch (CommunicationException ex) {
        		try {
        		    Thread.sleep(1000);
        		} catch (InterruptedException ex1) {
        		    break;
        		}
        	    }
        	}
            }
        
        public TaskMessageListener newTaskMessageListener(Server server) {
    	return new TaskMessageListener(server);
        }

    }

}
