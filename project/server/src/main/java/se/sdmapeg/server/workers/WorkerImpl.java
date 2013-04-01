package se.sdmapeg.server.workers;

import java.io.IOException;
import java.net.InetAddress;

import se.sdmapeg.common.communication.CommunicationException;

import se.sdmapeg.common.communication.Connection;
import se.sdmapeg.common.communication.ConnectionClosedException;
import se.sdmapeg.serverworker.ServerToWorkerMessage;
import se.sdmapeg.serverworker.WorkerToServerMessage;

public final class WorkerImpl implements Worker {

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
			throw new AssertionError(e);
		}
	}

	@Override
	public int getParallellWorkCapacity() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/**
	 * Creates a new Worker with the specified connection.
	 *
	 * @param connection A connection to a Worker.
	 * @return the Worker.
	 */
	public static WorkerImpl newWorker(Connection<ServerToWorkerMessage,
			WorkerToServerMessage> connection) {
		return new WorkerImpl(connection);
	}

}
