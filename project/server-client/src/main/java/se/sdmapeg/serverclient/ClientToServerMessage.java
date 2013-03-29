package se.sdmapeg.serverclient;

import se.sdmapeg.common.Message;

/**
 * A representation of a message from a Client to the Server.
 * 
 */
public interface ClientToServerMessage extends Message {
    	
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