package se.sdmapeg.serverclient.communication;

/**
 * Client-side "handshake" message, used to verify that the client is indeed a
 * client, and possibly send other relevant information to the server.
 */
public final class ClientIdentification implements ClientToServerMessage {
	private static final long serialVersionUID = 0;

	@Override
	public <T> T accept(Handler<T> handler) {
		return handler.handle(this);
	}

}
