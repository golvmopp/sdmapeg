package se.sdmapeg.worker;

import java.io.IOException;
import se.sdmapeg.serverworker.ServerToWorkerMessage;
import se.sdmapeg.serverworker.WorkerToServerMessage;

import java.net.InetAddress;

/**
 * Represents the server. Handles communication between Worker and Server.
 */
public interface Server {
    	
   	 /**
	 * returns the address of the server.
	 * @return the address of the server
	 */
	InetAddress getAddress();
	
	/**
	 * Sends a message from the worker to the server.
	 * @param message message to send
	 * @exception IOException if an error occurred
	 */
	void send(WorkerToServerMessage message) throws IOException;
	
	/**
	 * Receives a message from the server. This method blocks until a message has been received.
	 * @return received message.
	 * @exception IOException if an error occurred
	 */
	ServerToWorkerMessage receive() throws IOException;
	
	/**
	 * Disconnects from the server. If no connection is open, no action is performed.
	 */
	void disconnect();
}
