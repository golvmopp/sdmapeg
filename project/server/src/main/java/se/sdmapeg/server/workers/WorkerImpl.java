package se.sdmapeg.server.workers;

import java.io.IOException;
import java.net.InetAddress;

import se.sdmapeg.common.communication.Connection;
import se.sdmapeg.serverworker.ServerToWorkerMessage;
import se.sdmapeg.serverworker.WorkerToServerMessage;

public final class WorkerImpl implements Worker {

    private Connection<ServerToWorkerMessage, WorkerToServerMessage> connection;
    
    private WorkerImpl(Connection<ServerToWorkerMessage, 
	    WorkerToServerMessage> connection) {
	this.connection = connection;
    }
    
    public static WorkerImpl newWorker(Connection<ServerToWorkerMessage, 
	    WorkerToServerMessage> connection){
		return new WorkerImpl (connection);	
    }
    
    public InetAddress getAddress() {
	return connection.getAddress();
    }

    public void send(ServerToWorkerMessage message) throws IOException {
	connection.send(message);
    }

    public WorkerToServerMessage receive() throws IOException {
	return connection.receive();
    }

    public void disconnect() {
	try {
	    connection.close();
	} catch (IOException e) {
	    throw new AssertionError(e);
	}
    }

    public int getParallellWorkCapacity() {
	throw new UnsupportedOperationException("Not yet implemented");
    }

}
