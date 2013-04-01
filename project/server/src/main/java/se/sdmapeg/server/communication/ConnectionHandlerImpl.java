package se.sdmapeg.server.communication;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;

import se.sdmapeg.common.communication.CommunicationException;
import se.sdmapeg.common.communication.Connection;
import se.sdmapeg.common.communication.ConnectionImpl;

/**
 * Implementation of the ConnectionHandler interface.
 *
 */
public final class ConnectionHandlerImpl implements ConnectionHandler {

    private final ServerSocket serverSocket;
    
    private ConnectionHandlerImpl(int port) throws CommunicationException{
	try {
	    serverSocket = new ServerSocket(port);
	} catch (IOException e) {
	    throw new CommunicationException(e);
	}
    }
    
    
    @Override
    public void close() throws IOException {
	serverSocket.close();	
    }

    @Override
    public Connection accept() throws CommunicationException, SocketException {
	return ConnectionImpl.newConnection(serverSocket.accept());
	//TODO: Fix this whenever the exceptions here are sorted out. 
    }

    @Override
    public boolean isOpen() {
	return !serverSocket.isClosed();
    }
    
    public static ConnectionHandler newConnectionHandler(int port) throws CommunicationException{
	return new ConnectionHandlerImpl(port);	
    }
}
