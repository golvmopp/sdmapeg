package se.sdmapeg.server;

import se.sdmapeg.serverworker.ServerToWorkerMessage;
import se.sdmapeg.serverworker.WorkerToServerMessage;

import java.net.InetAddress;

public interface Worker {
	InetAddress getAddress();

	void send(ServerToWorkerMessage message);

	WorkerToServerMessage receive();

	void disconnect();

	int getParallellWorkCapacity();
}
