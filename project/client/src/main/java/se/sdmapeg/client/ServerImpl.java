package se.sdmapeg.client;

import java.io.IOException;
import java.net.InetAddress;

import se.sdmapeg.common.communication.CommunicationException;

import se.sdmapeg.common.communication.Connection;
import se.sdmapeg.serverclient.communication.ClientToServerMessage;
import se.sdmapeg.serverclient.communication.ServerToClientMessage;

public final class ServerImpl implements Server {
	private final Connection<ClientToServerMessage, ServerToClientMessage> connection;


	private ServerImpl(
			Connection<ClientToServerMessage, ServerToClientMessage> connection) {
		this.connection = connection;
	}

	@Override
	public InetAddress getAddress() {
		return connection.getAddress();
	}

	@Override
	public void send(ClientToServerMessage message) throws CommunicationException {
		connection.send(message);
	}

	@Override
	public ServerToClientMessage receive() throws CommunicationException {
		return connection.receive();
	}

	@Override
	public void disconnect() {
		try {
			connection.close();
		} catch (IOException ex) {
			// TODO Error logging here?
			throw new AssertionError(ex);
		}
	}

	/**
	 * Creates a new Server with the specified connection.
	 *
	 * @param connection A connection to a Server.
	 * @return the new Server.
	 */
	public static Server newServer(Connection<ClientToServerMessage, ServerToClientMessage> connection) {
		return new ServerImpl(connection);

	}

}
