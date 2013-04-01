package se.sdmapeg.common.communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * Implementation of a connection.
 */
public final class ConnectionImpl<S extends Message, R extends Message> implements Connection<S, R> {
	private final Socket socket;
	private final ObjectOutputStream output;
	private final ObjectInputStream input;

	private ConnectionImpl(Socket socket) throws CommunicationException {
		if (socket.isClosed() || !socket.isConnected()) {
			throw new CommunicationException("Socket must be open and connected.");
		}
		this.socket = socket;
		// To avoid deadlock, input and output must be initialized in this order.
		try {
			this.output = new ObjectOutputStream(socket.getOutputStream());
			this.input = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			try {
				socket.close();
			} catch (IOException ex) {
				// TODO: log this
				// Nothing to do here, http://fc06.deviantart.net/fs70/f/2011/288/3/c/nothing_to_do_here_by_rober_raik-d4cxltj.png
			}
			throw new CommunicationException(e);
		}
	}

	@Override
	public InetAddress getAddress() {
		return socket.getInetAddress();
	}

	@Override
	public void send(S message) throws CommunicationException,
			ConnectionClosedException {
		synchronized (output) {
			try {
				output.writeObject(message);
			} catch (SocketException ex) {
				throw new ConnectionClosedException(ex);
			}
			catch (IOException e) {
				throw new CommunicationException(e);
			}
		}
	}

	@Override
	public R receive() throws CommunicationException {
		synchronized (input) {
			try {
				return (R) input.readObject();
			} catch (SocketException ex) {
				throw new ConnectionClosedException(ex);
			} catch (IOException | ClassNotFoundException e) {
				throw new CommunicationException(e);
			}
		}
	}

	@Override
	public boolean isOpen() {
		return !socket.isClosed();
	}

	@Override
	public void close() throws IOException {
		socket.close();
	}

	/**
	 * Creates a new connection communicating over the specified socket. The socket must be connected and open.
	 *
	 * @param socket The socket to communicate over.
	 * @return The new connection
	 * @throws CommunicationException if socket is not open and connected or an error occurs during initialisation.
	 */
	public static Connection newConnection(Socket socket) throws CommunicationException {
		return new ConnectionImpl(socket);
	}
}
