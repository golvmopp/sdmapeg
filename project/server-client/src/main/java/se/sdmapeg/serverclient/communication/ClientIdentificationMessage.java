package se.sdmapeg.serverclient.communication;

/**
 * Client-side "handshake" message, used to verify that the client is indeed a
 * client, and possibly send other relevant information to the server.
 */
public interface ClientIdentificationMessage extends ClientToServerMessage {
	@Override
	<T> T accept(Handler<T> handler);
}
