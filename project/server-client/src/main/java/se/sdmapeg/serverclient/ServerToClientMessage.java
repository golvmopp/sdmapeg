package se.sdmapeg.serverclient;

import se.sdmapeg.common.communication.Message;



/**
 * A representation of a message from the Server to a Client.
 * 
 */
public interface ServerToClientMessage extends Message {
    	
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
