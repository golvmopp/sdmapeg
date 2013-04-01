package se.sdmapeg.server.clients;

import java.io.IOException;
import java.net.InetAddress;

import se.sdmapeg.common.communication.CommunicationException;

import se.sdmapeg.common.communication.Connection;
import se.sdmapeg.serverclient.communication.ClientToServerMessage;
import se.sdmapeg.serverclient.communication.ServerToClientMessage;

public final class ClientImpl implements Client {

	private final Connection<ServerToClientMessage,
			ClientToServerMessage> connection;

	private ClientImpl(Connection<ServerToClientMessage,
			ClientToServerMessage> connection) {
		this.connection = connection;
	}

	@Override
	public InetAddress getAddress() {
		return connection.getAddress();
	}

	@Override
	public void send(ServerToClientMessage message) throws CommunicationException {
		connection.send(message);
	}

	@Override
	public ClientToServerMessage receive() throws CommunicationException {
		return connection.receive();
	}

	@Override
	public void disconnect() {
		try {
			connection.close();
		} catch (IOException e) {
			// TODO: Log this
			throw new AssertionError(e);
		}
	}

	/**
	 * Creates a new Client with the specified connection.
	 *
	 * @param connection A connection to a Client.
	 * @return the Client.
	 */
	public static Client newClient(Connection<ServerToClientMessage,
			ClientToServerMessage> connection) {
		return new ClientImpl(connection);
	}

}
