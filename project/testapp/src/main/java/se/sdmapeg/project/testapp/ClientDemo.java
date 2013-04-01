package se.sdmapeg.project.testapp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author niclas
 */
public class ClientDemo {
	public Connection<ClientMessage, ServerMessage> connectTo(
			InetSocketAddress server) throws IOException {
		Socket socket = new Socket();
		socket.connect(server);
		return new Connection<>(socket);
	}
}
