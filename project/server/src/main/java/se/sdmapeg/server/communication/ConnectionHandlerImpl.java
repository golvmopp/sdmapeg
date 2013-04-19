package se.sdmapeg.server.communication;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;

import se.sdmapeg.common.communication.CommunicationException;
import se.sdmapeg.common.communication.Connection;
import se.sdmapeg.common.communication.ConnectionImpl;
import se.sdmapeg.common.communication.Message;

/**
 * Implementation of the ConnectionHandler interface.
 *
 */
public final class ConnectionHandlerImpl<S extends Message, R extends Message>
		implements ConnectionHandler<S, R> {
	private final ServerSocket serverSocket;

	private ConnectionHandlerImpl(int port) throws CommunicationException {
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
	public Connection<S, R> accept() throws CommunicationException,
											SocketException {
		try {
			return ConnectionImpl.newConnection(serverSocket.accept());
		} catch (SocketException e) {
			throw e;
		} catch(IOException e) {
			throw new CommunicationException(e);
		}
	}

	@Override
	public boolean isOpen() {
		return !serverSocket.isClosed();
	}

	/**
	 * Creates a new ConnectionHandler listening for connections on the
	 * specified port.
	 *
	 * @param <S>	the type of messages that can be sent over the connections
	 *				accepted by the created ConnectionHandler
	 * @param <R>	the type of messages that can be received over the
	 *				connections accepted by the created ConnectionHandler
	 * @param port the port to listen for connections on
	 * @return the created ConnectionHandler
	 * @throws CommunicationException	if something went wrong when attempting
	 *									to create the ConnectionHandler
	 */
	public static <S extends Message, R extends Message> ConnectionHandler<S, R>
			newConnectionHandler(int port) throws CommunicationException {
		return new ConnectionHandlerImpl<>(port);
	}
}
