package se.sdmapeg.client;

import se.sdmapeg.serverclient.ClientToServerMessage;
import se.sdmapeg.serverclient.ServerToClientMessage;

import java.net.InetAddress;

public interface Server {
	InetAddress getAddress();

	void send(ClientToServerMessage message);

	ServerToClientMessage receive();

	void disconnect();
}
