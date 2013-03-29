package se.sdmapeg.serverworker;

import se.sdmapeg.common.Message;
/**
 * A representation of a message from the Server to a Worker.
 * 
 */
public interface ServerToWorkerMessage extends Message {
    
    	/**
    	 * Accept method for use with the visitor pattern.
    	 */
	<T> T accept(Visitor<T> visitor);
	
	/**
	 * A visitor for messages from Server to Worker.
	 */
	interface Visitor<T> {

	}
}
