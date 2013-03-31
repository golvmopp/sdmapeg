package se.sdmapeg.client;

import java.io.IOException;
import java.net.InetAddress;

import se.sdmapeg.common.communication.Connection;
import se.sdmapeg.serverclient.communication.ClientToServerMessage;
import se.sdmapeg.serverclient.communication.ServerToClientMessage;

public final class ServerImpl implements Server {
    private final Connection<ClientToServerMessage, ServerToClientMessage> connection;
    
    private ServerImpl(
	    Connection<ClientToServerMessage, ServerToClientMessage> connection) {
	this.connection = connection;
    }
    
    public InetAddress getAddress() {
	return connection.getAddress();
    }

    public void send(ClientToServerMessage message) throws IOException {
	connection.send(message);
    }

    public ServerToClientMessage receive() throws IOException {
	return connection.receive();
    }

    public void disconnect() {
	try {
	    connection.close();
	} catch (IOException ex) {
	    // TODO Error logging here?
	    throw new AssertionError(ex);
	}
    }
    
    public static Server newServer(Connection<ClientToServerMessage, ServerToClientMessage> connection) {
	return new ServerImpl(connection);
	
    }

}
