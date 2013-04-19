package se.sdmapeg.serverworker.communication;

import se.sdmapeg.common.communication.Message;

/**
 * A representation of a message from the Server to a Worker.
 */
public interface ServerToWorkerMessage extends Message {

	/**
	 * Accept method for use with the visitor pattern.
	 */
	<T> T accept(Handler<T> handler);

	/**
	 * A handler for messages from Server to Worker.
	 */
	interface Handler<T> {
	    /**
	     * Handles a TaskMessage.
	     * @param message The message to handle.
	     */
	    T handle(TaskMessage message);

	    T handle(WorkStealingRequest message);

	}
}
