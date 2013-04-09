package se.sdmapeg.worker;

import se.sdmapeg.common.communication.Connection;
import se.sdmapeg.serverworker.ServerToWorkerMessage;
import se.sdmapeg.serverworker.WorkerToServerMessage;

import java.io.IOException;
import java.net.InetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sdmapeg.common.communication.CommunicationException;
import se.sdmapeg.common.communication.ConnectionClosedException;

/**
 * Implementation of Server interface.
 */
public final class ServerImpl implements Server {
	private static final Logger LOG = LoggerFactory.getLogger(ServerImpl.class);
	private final Connection<WorkerToServerMessage, ServerToWorkerMessage> connection;

	private ServerImpl(Connection<WorkerToServerMessage,
			ServerToWorkerMessage> connection) {
		this.connection = connection;
	}

	@Override
	public InetAddress getAddress() {
		return connection.getAddress();
	}

	@Override
	public void send(WorkerToServerMessage message)
			throws CommunicationException, ConnectionClosedException {
		connection.send(message);
	}

	@Override
	public ServerToWorkerMessage receive() throws CommunicationException,
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
	 * Creates a new Server with the specified connection.
	 *
	 * @param connection A connection to a Server.
	 * @return the Server.
	 */
	public static Server newServer(Connection<WorkerToServerMessage,
			ServerToWorkerMessage> connection) {
		return new ServerImpl(connection);
	}
}
