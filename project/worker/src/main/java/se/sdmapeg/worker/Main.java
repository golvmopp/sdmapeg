package se.sdmapeg.worker;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;

import se.sdmapeg.common.communication.CommunicationException;
import se.sdmapeg.common.communication.ConnectionImpl;

public class Main {
	private static final int SERVER_PORT = 6667;

	/**
	 * @param args
	 * @throws CommunicationException
	 */
	public static void main(String[] args) throws CommunicationException {
		String host = JOptionPane.showInputDialog("Address:", "server.sdmapeg.se");
		Server server;
		try {
			server = ServerImpl.newServer(ConnectionImpl
					.newConnection(new Socket(host, SERVER_PORT)));
		} catch (CommunicationException | IOException e) {
			throw new CommunicationException();
		}
		int poolSize = Runtime.getRuntime().availableProcessors();
		Worker worker = WorkerImpl.newWorkerImpl(poolSize, server,
												new TaskPerformerImpl());
		worker.start();
		JOptionPane.showMessageDialog(null, "Worker running...");
		worker.stop();
	}
}
