package se.sdmapeg.project.testapp;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @author niclas
 */
public final class Connection<S extends Message, R extends Message>
		implements Closeable {
	private final Socket socket;
	private final ObjectInputStream input;
	private final ObjectOutputStream output;

	public Connection(Socket socket) throws IOException {
		this.socket = socket;
		output = new ObjectOutputStream(socket.getOutputStream());
		input = new ObjectInputStream(socket.getInputStream());
	}

	public void sendMessage(S message) throws IOException {
		output.writeObject(message);
		output.flush();
	}

	public R receiveMessage() throws IOException {
		try {
			return (R) input.readObject();
		} catch (ClassNotFoundException ex) {
			throw new AssertionError(ex);
		}
	}

	@Override
	public void close() throws IOException {
		input.close();
		output.close();
		socket.close();
	}
}
