package se.sdmapeg.worker;

import se.sdmapeg.serverworker.ServerToWorkerMessage;
import se.sdmapeg.serverworker.WorkerToServerMessage;

import java.net.InetAddress;

public interface Server {
	InetAddress getAddress();

	void send(WorkerToServerMessage message);

	ServerToWorkerMessage receive();

	void disconnect();
}
