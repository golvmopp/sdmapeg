package se.sdmapeg.serverworker.communication;

import se.sdmapeg.common.communication.Message;

/**
 * A representation of a message from a Worker to the Server.
 */
public interface WorkerToServerMessage extends Message {

	/**
	 * Accept method for use with the visitor pattern.
	 */
	<T> T accept(Handler<T> handler);

	/**
	 * A handler for messages from Worker to Server.
	 */
	interface Handler<T> {

	    /**
	     * Handles a ResultMessage.
	     * @param message The message to handle.
	     */
	    T handle(ResultMessage message);

		/**
		 * Handles a WorkerIdentificationMessage.
		 *
		 * @param message the message to handle
		 */
		T handle(WorkerIdentificationMessage message);

		T handle(WorkStealingResponseMessage message);
	}
}
