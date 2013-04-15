package se.sdmapeg.server.workers;

import java.io.IOException;
import java.net.InetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sdmapeg.common.communication.CommunicationException;

import se.sdmapeg.common.communication.Connection;
import se.sdmapeg.common.communication.ConnectionClosedException;
import se.sdmapeg.serverworker.ServerToWorkerMessage;
import se.sdmapeg.serverworker.WorkerToServerMessage;

public final class WorkerImpl implements Worker {
	private static final Logger LOG = LoggerFactory.getLogger(WorkerImpl.class);
	private Connection<ServerToWorkerMessage, WorkerToServerMessage> connection;

	private WorkerImpl(Connection<ServerToWorkerMessage,
			WorkerToServerMessage> connection) {
		this.connection = connection;
	}

	@Override
	public InetAddress getAddress() {
		return connection.getAddress();
	}

	@Override
	public void send(ServerToWorkerMessage message)
			throws CommunicationException, ConnectionClosedException {
		connection.send(message);
	}

	@Override
	public WorkerToServerMessage receive() throws CommunicationException,
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

	@Override
	public int getParallellWorkCapacity() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public String toString() {
		return "Worker{" + connection.getAddress() + '}';
	}

	/**
	 * Creates a new Worker with the specified connection.
	 *
	 * @param connection A connection to a Worker.
	 * @return the Worker.
	 */
	public static Worker newWorker(Connection<ServerToWorkerMessage,
			WorkerToServerMessage> connection) {
		return new WorkerImpl(connection);
	}

}
