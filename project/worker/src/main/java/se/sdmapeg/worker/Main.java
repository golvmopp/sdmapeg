package se.sdmapeg.worker;

import java.io.IOException;
import java.net.Socket;

import javax.swing.JOptionPane;

import se.sdmapeg.common.communication.CommunicationException;
import se.sdmapeg.common.communication.Connection;
import se.sdmapeg.common.communication.ConnectionImpl;
import se.sdmapeg.serverworker.communication.ServerToWorkerMessage;
import se.sdmapeg.serverworker.communication.WorkerToServerMessage;
import se.sdmapeg.worker.gui.WorkerView;

public final class Main {
	private static final int SERVER_PORT = 6667;

	private Main() {
		// Prevent instantiation
		throw new AssertionError();
	}
	/**
	 * @param args
	 * @throws CommunicationException
	 */
	public static void main(String[] args) throws CommunicationException {
		String host = JOptionPane.showInputDialog("Address:", "server.sdmapeg.se");
		Server server;
		try {
			Connection<WorkerToServerMessage, ServerToWorkerMessage> connection =
				ConnectionImpl.newConnection(new Socket(host, SERVER_PORT));
			server = ServerImpl.newServer(connection);
		} catch (CommunicationException | IOException e) {
			throw new CommunicationException();
		}
		int poolSize = Runtime.getRuntime().availableProcessors();
		Worker worker = WorkerImpl.newWorkerImpl(poolSize, server, host,
												new TaskPerformerImpl());
		worker.start();
		WorkerView view = new WorkerView(worker);
	}
}
