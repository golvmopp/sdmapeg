package se.sdmapeg.server;

import se.sdmapeg.serverclient.ClientToServerMessage;
import se.sdmapeg.serverclient.ServerToClientMessage;

import java.net.InetAddress;

public interface Client {
	InetAddress getAddress();

	void send(ServerToClientMessage message);

	ClientToServerMessage receive();

	void disconnect();
}
