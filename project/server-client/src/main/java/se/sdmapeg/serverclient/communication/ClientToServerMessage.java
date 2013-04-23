package se.sdmapeg.serverclient.communication;

import se.sdmapeg.common.communication.Message;


/**
 * A representation of a message from a Client to the Server.
 */
public interface ClientToServerMessage extends Message {

	/**
	 * Accept method for use with the visitor pattern.
	 */
	<T> T accept(Handler<T> handler);

	/**
	 * A handler for messages from Server to Worker.
	 */
	interface Handler<T> {
		T handle(ClientIdentificationMessage message);

		T handle(TaskMessage message);

		T handle(TaskCancellationMessage mesage);
	}
}
