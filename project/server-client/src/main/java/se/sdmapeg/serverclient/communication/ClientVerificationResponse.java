package se.sdmapeg.serverclient.communication;

/**
 * Client-side "handshake" message, used to verify that the client is indeed a client, and possibly send other relevant information to the server. 
 */
public final class ClientVerificationResponse implements ClientToServerMessage {
    private static final long serialVersionUID = 0;

    public <T> T accept(Visitor<T> visitor) {
	return visitor.visit(this);
    }

}
