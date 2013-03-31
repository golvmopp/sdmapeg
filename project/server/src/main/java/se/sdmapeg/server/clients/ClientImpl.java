package se.sdmapeg.server.clients;

import java.io.IOException;
import java.net.InetAddress;

import se.sdmapeg.common.communication.Connection;
import se.sdmapeg.serverclient.ClientToServerMessage;
import se.sdmapeg.serverclient.ServerToClientMessage;

public final class ClientImpl implements Client {

    private final Connection<ServerToClientMessage, 
    	ClientToServerMessage> connection;
    
    private ClientImpl(Connection<ServerToClientMessage, 
	    	ClientToServerMessage> connection) {
	this.connection = connection;
    }
    
    public static Client newClient(Connection<ServerToClientMessage, 
	    	ClientToServerMessage> connection) {
	return new ClientImpl(connection);
    }
    
    public InetAddress getAddress() {
	return connection.getAddress();
    }

    public void send(ServerToClientMessage message) throws IOException {
	connection.send(message);
    }

    public ClientToServerMessage receive() throws IOException {
	return connection.receive();
    }

    public void disconnect() {
	try {
	    connection.close();
	} catch (IOException e) {
	    // TODO: Log this
	    throw new AssertionError(e);
	}
    }

}
