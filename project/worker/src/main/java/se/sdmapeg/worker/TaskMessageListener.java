package se.sdmapeg.worker;

import se.sdmapeg.common.communication.CommunicationException;
import se.sdmapeg.common.communication.ConnectionClosedException;
import se.sdmapeg.common.communication.Message;
import se.sdmapeg.serverworker.*;

/**
 * A thread that concurrently checks for new task messages from the server.
 *
 */
public final class TaskMessageListener implements Runnable {
    
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
			    taskMessage.getTask();
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
