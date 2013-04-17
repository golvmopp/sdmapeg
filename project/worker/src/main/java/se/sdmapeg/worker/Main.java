package se.sdmapeg.worker;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;

import se.sdmapeg.common.communication.CommunicationException;
import se.sdmapeg.common.communication.ConnectionImpl;

public class Main {

    /**
     * @param args
     * @throws CommunicationException 
     */
    public static void main(String[] args) throws CommunicationException {
	String host = JOptionPane.showInputDialog("Address:");
	String portString = JOptionPane.showInputDialog("Port:");
	Server server;
	
	
	while (!portString.matches("\\d+")) {
		JOptionPane.showMessageDialog(null, "Invalid port");
		portString = JOptionPane.showInputDialog("Port:");
	}
	int port = Integer.parseInt(portString);
	try {
	    server = ServerImpl.newServer(ConnectionImpl.newConnection
		    (new Socket (host, port)));
	} catch (CommunicationException|IOException e) {
		throw new CommunicationException();
	}
	int poolSize = Runtime.getRuntime().availableProcessors();
	Worker worker = WorkerImpl.newWorkerImpl(poolSize, server,
		new TaskPerformerImpl());
    }

}
