package se.sdmapeg.server.clients;

import java.io.IOException;
import java.net.InetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sdmapeg.common.communication.CommunicationException;
import se.sdmapeg.common.communication.Connection;
import se.sdmapeg.common.communication.ConnectionClosedException;
import se.sdmapeg.serverclient.communication.ClientToServerMessage;
import se.sdmapeg.serverclient.communication.ServerToClientMessage;

final class ClientImpl implements Client {
	private static final Logger LOG = LoggerFactory.getLogger(ClientImpl.class);

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
	public void send(ServerToClientMessage message)
			throws CommunicationException, ConnectionClosedException {
		connection.send(message);
	}

	@Override
	public ClientToServerMessage receive() throws CommunicationException,
			ConnectionClosedException {
		return connection.receive();
	}

	@Override
	public void disconnect() {
		try {
			connection.close();
		} catch (IOException e) {
			LOG.warn("An error occurred while closing the connection", e);
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

	@Override
	public String toString() {
		return "Client{" + connection.getAddress() + '}';
	}
}
