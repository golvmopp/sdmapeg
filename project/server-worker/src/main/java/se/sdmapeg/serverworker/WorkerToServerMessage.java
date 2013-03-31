package se.sdmapeg.serverworker;

import se.sdmapeg.common.communication.Message;

/**
 * A representation of a message from a Worker to the Server.
 * 
 */
public interface WorkerToServerMessage extends Message {

	/**
	 * Accept method for use with the visitor pattern.
	 */
    	<T> T accept(Visitor<T> visitor);

    	/**
	 * A visitor for messages from Worker to Server.
	 */
	interface Visitor<T> {

	}
}
