package se.sdmapeg.serverclient.communication;

import se.sdmapeg.common.communication.Message;


/**
 * A representation of a message from the Server to a Client.
 */
public interface ServerToClientMessage extends Message {

	/**
	 * Accept method for use with the visitor pattern.
	 */
	<T> T accept(Handler<T> handler);

	/**
	 * A handler for messages from Server to Worker.
	 */
	interface Handler<T> {
		/**
		 * Handles a ResultMessage.
		 * @param message The message to handle.
		 */
		T handle(ResultMessage message);
	}
}
