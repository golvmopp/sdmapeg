package se.sdmapeg.common.communication;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a connection.
 */
public final class ConnectionImpl<S extends Message, R extends Message>
		implements Connection<S, R> {
	private static final Logger LOG = LoggerFactory.getLogger(ConnectionImpl.class);
	private final Socket socket;
	private final InetSocketAddress address;
	private final ObjectOutputStream output;
	private final ObjectInputStream input;

	private ConnectionImpl(Socket socket) throws CommunicationException {
		if (socket.isClosed() || !socket.isConnected()) {
			throw new CommunicationException(
					"Socket must be open and connected.");
		}
		this.socket = socket;
		this.address = new InetSocketAddress(socket.getInetAddress(),
			socket.getPort());
		try {
			/*
			 * During construction, ObjectOutputStream writes a serialization
			 * header to the underlying output stream, which is then read in the
			 * constructor of ObjectInputStream.
			 * 
			 * Since reading from the stream blocks if no data is available, and
			 * this class might be used on both ends of the connection, it is
			 * important that the ObjectOutputStream is initialised before the
			 * ObjectInputStream, or we will find ourselves in a deadlock with
			 * both ends waiting for each other to send the serialization header
			 * before proceeding to send their own.
			 */
			// First construct the output stream
			this.output = new ObjectOutputStream(socket.getOutputStream());
			// Flush the stream, to ensure that the header is indeed sent
			output.flush();
			// Then construct the input stream, after the header has been sent
			this.input = new ObjectInputStream(socket.getInputStream());
		}
		catch (IOException e) {
			try {
				socket.close();
			}
			catch (IOException ex) {
				LOG.warn("An error occurred while closing the connection", ex);
			}
			throw new CommunicationException(e);
		}
	}

	@Override
	public InetSocketAddress getAddress() {
		return address;
	}

	@Override
	public void send(S message) throws CommunicationException,
									   ConnectionClosedException {
		try {
			synchronized (output) {
				output.writeObject(message);
				// Make sure that the message is sent immediately
				output.flush();
			}
		}
		catch (SocketException ex) {
			try {
				close();
			} finally {
				throw new ConnectionClosedException(ex);
			}
		} catch (IOException ex) {
			throw new CommunicationException(ex);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public R receive() throws CommunicationException,
			ConnectionClosedException {
		try {
			synchronized (input) {
				return (R) input.readObject();
			}
		} catch (SocketException | EOFException ex) {
			try {
				close();
			} finally {
				throw new ConnectionClosedException(ex);
			}
		} catch (IOException | ClassNotFoundException e) {
			throw new CommunicationException(e);
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
	 * Creates a new connection communicating over the specified socket. The
	 * socket must be connected and open.
	 *
	 * @param socket The socket to communicate over.
	 * @return The new connection
	 * @throws CommunicationException	if socket is not open and connected or
	 *									an error occurs during initialisation.
	 */
	public static <S extends Message, R extends Message> Connection<S, R>
			newConnection(Socket socket) throws CommunicationException {
		return new ConnectionImpl<>(socket);
	}
}
