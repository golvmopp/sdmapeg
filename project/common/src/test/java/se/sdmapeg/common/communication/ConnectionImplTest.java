package se.sdmapeg.common.communication;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author niclas
 */
public class ConnectionImplTest {
	private static final String LOCALHOST = "127.0.0.1";
	private static final int RUNS = 10000;

	public ConnectionImplTest() {
	}

	/**
	 * Test of getAddress method, of class ConnectionImpl.
	 */
	@Test
	public void testGetAddress() throws Exception {
		InetAddress address = InetAddress.getByName(LOCALHOST);
		try (Sockets sockets = new Sockets(6666)) {
			Socket server = sockets.getServer();
			Socket client = sockets.getClient();
			new ObjectOutputStream(server.getOutputStream());
			Connection<MockMessage, MockMessage> clientConnection =
				ConnectionImpl.newConnection(client);
			assertEquals(address, clientConnection.getAddress());
		}
	}

	/**
	 * Test of send method, of class ConnectionImpl.
	 */
	@Test
	public void testSend() throws Exception {
		try (Sockets sockets = new Sockets(6666)) {
			Socket server = sockets.getServer();
			Socket client = sockets.getClient();
			new ObjectOutputStream(server.getOutputStream());
			Connection<MockMessage, MockMessage> clientConnection =
				ConnectionImpl.newConnection(client);
			ObjectInputStream serverInputStream = new ObjectInputStream(
					server.getInputStream());
			Random rng = new Random(-1L);
			for (int i = 0; i < RUNS; i++) {
				long data = rng.nextLong();
				MockMessage message = new MockMessage(data);
				clientConnection.send(message);
				Object readObject = serverInputStream.readObject();
				assertEquals(message, readObject);
			}
		}
	}

	@Test
	public void testSendIOException() throws Exception {
		try (Sockets sockets = new Sockets(6666)) {
			Socket server = sockets.getServer();
			Socket client = sockets.getClient();
			new ObjectOutputStream(server.getOutputStream());
			Connection<FailedMessage, FailedMessage> clientConnection =
				ConnectionImpl.newConnection(client);
			ObjectInputStream serverInputStream = new ObjectInputStream(
					server.getInputStream());
			FailedMessage message =
				new FailedMessage(FailedMessage.Type.SEND_EXCEPTION);
			try {
				clientConnection.send(message);
				fail("Receiving an IOException while sending a message should"
						+ " throw an exception");
			} catch (CommunicationException ex) {
				assertEquals(CommunicationException.class, ex.getClass());
			}
		}
	}

	/**
	 * Test of receive method, of class ConnectionImpl.
	 */
	@Test
	public void testReceive() throws Exception {
		try (Sockets sockets = new Sockets(6666)) {
			Socket server = sockets.getServer();
			Socket client = sockets.getClient();
			ObjectOutputStream serverOutputStream = new ObjectOutputStream(
					server.getOutputStream());
			Connection<MockMessage, MockMessage> clientConnection =
				ConnectionImpl.newConnection(client);
			Random rng = new Random(-1L);
			for (int i = 0; i < RUNS; i++) {
				long data = rng.nextLong();
				MockMessage message = new MockMessage(data);
				serverOutputStream.writeObject(message);
				MockMessage receivedMessage = clientConnection.receive();
				assertEquals(message, receivedMessage);
			}
		}
	}

	@Test
	public void testReceiveIOException() throws Exception {
		try (Sockets sockets = new Sockets(6666)) {
			Socket server = sockets.getServer();
			Socket client = sockets.getClient();
			ObjectOutputStream serverOutputStream = new ObjectOutputStream(
					server.getOutputStream());
			Connection<FailedMessage, FailedMessage> clientConnection =
				ConnectionImpl.newConnection(client);
			FailedMessage message =
				new FailedMessage(FailedMessage.Type.RECEIVE_EXCEPTION);
			try {
				serverOutputStream.writeObject(message);
				clientConnection.receive();
				fail("Receiving an IOException while receiving a message should"
						+ " throw an exception");
			} catch (CommunicationException ex) {
			}
		}
	}

	@Test
	public void testReceiveClassNotFoundException() throws Exception {
		try (Sockets sockets = new Sockets(6666)) {
			Socket server = sockets.getServer();
			Socket client = sockets.getClient();
			ObjectOutputStream serverOutputStream = new ObjectOutputStream(
					server.getOutputStream());
			Connection<FailedMessage, FailedMessage> clientConnection =
				ConnectionImpl.newConnection(client);
			FailedMessage message =
				new FailedMessage(FailedMessage.Type.RECEIVE_CLASS_NOT_FOUND);
			try {
				serverOutputStream.writeObject(message);
				clientConnection.receive();
				fail("Receiving a ClassNotFoundException while receiving a"
						+ " message should throw an exception");
			} catch (CommunicationException ex) {
			}
		}
	}

	/**
	 * Test of isOpen method, of class ConnectionImpl.
	 */
	@Test
	public void testIsOpen() throws Exception {
		try (Sockets sockets = new Sockets(6666)) {
			Socket server = sockets.getServer();
			Socket client = sockets.getClient();
			new ObjectOutputStream(server.getOutputStream());
			Connection<MockMessage, MockMessage> clientConnection =
				ConnectionImpl.newConnection(client);
			assertTrue("Connection should be open when created",
					   clientConnection.isOpen());
			clientConnection.close();
			assertFalse("Connection should not be open after being closed",
					   clientConnection.isOpen());
		}
	}

	/**
	 * Test of close method, of class ConnectionImpl.
	 */
	@Test
	public void testClose() throws Exception {
		try (Sockets sockets = new Sockets(6666)) {
			Socket server = sockets.getServer();
			Socket client = sockets.getClient();
			new ObjectOutputStream(server.getOutputStream());
			Connection<MockMessage, MockMessage> clientConnection =
				ConnectionImpl.newConnection(client);
			try {
				clientConnection.close();
				clientConnection.close();
			} catch (IOException ex) {
				fail("Repeatedly closing a connection should not throw any exception");
			}
			assertFalse("Connection should not be open after being closed",
					   clientConnection.isOpen());
		}
	}

	/**
	 * Test of newConnection method, of class ConnectionImpl.
	 */
	@Test
	public void testNewConnection() throws Exception {
		try (Sockets sockets = new Sockets(6666)) {
			Socket server = sockets.getServer();
			Socket client = sockets.getClient();
			new ObjectOutputStream(server.getOutputStream());
			Connection<MockMessage, MockMessage> clientConnection =
				ConnectionImpl.newConnection(client);
			assertTrue("Connection should be open when created",
					   clientConnection.isOpen());
		}
		catch (IOException | CommunicationException ex) {
			fail("Creating a connection with an open and connected socket should not fail");
		}
	}

	@Test
	public void testSendThisEndCosed() throws Exception {
		try (Sockets sockets = new Sockets(6666)) {
			Socket server = sockets.getServer();
			Socket client = sockets.getClient();
			new ObjectOutputStream(server.getOutputStream());
			Connection<MockMessage, MockMessage> clientConnection =
				ConnectionImpl.newConnection(client);
			ObjectInputStream serverInputStream = new ObjectInputStream(
					server.getInputStream());
			clientConnection.close();
			long data = -1L;
			MockMessage message = new MockMessage(data);
			try {
				clientConnection.send(message);
				fail("Sending a message through a closed connection should"
						+ " throw an exception");
			} catch (CommunicationException ex) {
				assertTrue("Attempting to send a message through a closed"
						+ " connection should throw a ConnectionClosedException",
					ex instanceof ConnectionClosedException);
			}
		}
	}

	//@Test
	public void testSendOtherEndClosed() throws Exception {
		try (Sockets sockets = new Sockets(6666)) {
			Socket server = sockets.getServer();
			Socket client = sockets.getClient();
			new ObjectOutputStream(server.getOutputStream());
			Connection<MockMessage, MockMessage> clientConnection =
				ConnectionImpl.newConnection(client);
			ObjectInputStream serverInputStream = new ObjectInputStream(
					server.getInputStream());
			server.close();
			long data = -1L;
			MockMessage message = new MockMessage(data);
			try {
				clientConnection.send(message);
				fail("Sending a message through a closed connection should"
						+ " throw an exception");
			} catch (CommunicationException ex) {
				assertTrue("Attempting to send a message through a closed"
						+ " connection should throw a ConnectionClosedException",
					ex instanceof ConnectionClosedException);
			}
		}
	}

	@Test
	public void testReceiveThisEndClosed() throws Exception {
		try (Sockets sockets = new Sockets(6666)) {
			Socket server = sockets.getServer();
			Socket client = sockets.getClient();
			new ObjectOutputStream(server.getOutputStream());
			Connection<MockMessage, MockMessage> clientConnection =
				ConnectionImpl.newConnection(client);
			clientConnection.close();
			try {
				clientConnection.receive();
				fail("Listening for a message on a closed connection should"
						+ " throw an exception");
			} catch (CommunicationException ex) {
				assertTrue("Listening for a message on a closed"
						+ " connection should throw a ConnectionClosedException",
					ex instanceof ConnectionClosedException);
			}
		}
	}

	@Test
	public void testReceiveOtherEndClosed() throws Exception {
		try (Sockets sockets = new Sockets(6666)) {
			Socket server = sockets.getServer();
			Socket client = sockets.getClient();
			new ObjectOutputStream(server.getOutputStream());
			Connection<MockMessage, MockMessage> clientConnection =
				ConnectionImpl.newConnection(client);
			server.close();
			try {
				clientConnection.receive();
				fail("Listening for a message on a closed connection should"
						+ " throw an exception");
			} catch (CommunicationException ex) {
				assertTrue("Listening for a message on a closed"
						+ " connection should throw a ConnectionClosedException",
					ex instanceof ConnectionClosedException);
			}
		}
	}

	@Test
	public void testBlockingReceiveCloseThisEnd() throws Exception {
		try (Sockets sockets = new Sockets(6666)) {
			Socket server = sockets.getServer();
			Socket client = sockets.getClient();
			new ObjectOutputStream(server.getOutputStream());
			final Connection<MockMessage, MockMessage> clientConnection =
				ConnectionImpl.newConnection(client);
			final Thread testThread = Thread.currentThread();
			Thread connectionCloser = new Thread(new ConnectionCloser(testThread, clientConnection));
			connectionCloser.start();
			try {
				clientConnection.receive();
				fail("Blocking while listening to a connection should throw an"
						+ " exception when the connection is closed");
			} catch (CommunicationException ex) {
				assertTrue("Blocking while listening to a connection should"
						+ " throw a ConnectionClosedException when the"
						+ " connection is closed",
					ex instanceof ConnectionClosedException);
			}
		}
	}

	@Test
	public void testBlockingReceiveCloseOtherEnd() throws Exception {
		try (Sockets sockets = new Sockets(6666)) {
			final Socket server = sockets.getServer();
			Socket client = sockets.getClient();
			new ObjectOutputStream(server.getOutputStream());
			Connection<MockMessage, MockMessage> clientConnection =
				ConnectionImpl.newConnection(client);
			final Thread testThread = Thread.currentThread();
			Thread connectionCloser = new Thread(new ConnectionCloser(testThread, server));
			connectionCloser.start();
			try {
				clientConnection.receive();
				fail("Blocking while listening to a connection should throw an"
						+ " exception when the connection is closed");
			} catch (CommunicationException ex) {
				assertTrue("Blocking while listening to a connection should"
						+ " throw a ConnectionClosedException when the"
						+ " connection is closed",
					ex instanceof ConnectionClosedException);
			}
		}
	}

	@Test
	public void testNewServerSocketConnection() {
		try (Sockets sockets = new Sockets(6666)) {
			Socket server = sockets.getServer();
			Socket client = sockets.getClient();
			new ObjectOutputStream(client.getOutputStream());
			Connection<MockMessage, MockMessage> clientConnection =
				ConnectionImpl.newConnection(server);
			assertTrue("Server connection should be open when created",
					   clientConnection.isOpen());
		} catch (IOException | CommunicationException ex) {
			fail("Creating a connection with a socket from a ServerSocket"
					+ " should work just like a client socket");
		}
	}

	@Test
	public void testNewUnconnectedConnection() {
		try (Connection<MockMessage, MockMessage> clientConnection =
				ConnectionImpl.newConnection(new Socket())) {
			fail("Creating a connection with an unconnected socket should fail");
		} catch (IOException | CommunicationException ex) {
			assertTrue("Creating a connection with an unconnected socket"
					+ " should throw a CommunicationException",
					ex instanceof CommunicationException);
		}
	}

	@Test
	public void testNewThisEndDisconnectedConnection() {
		try (Sockets sockets = new Sockets(6666)) {
			Socket server = sockets.getServer();
			Socket client = sockets.getClient();
			new ObjectOutputStream(server.getOutputStream());
			// Close our end
			client.close();
			ConnectionImpl.newConnection(client);
			fail("Creating a connection with an unconnected socket should fail");
		} catch (IOException | CommunicationException ex) {
			assertTrue("Creating a connection with an unconnected socket"
					+ " should throw a CommunicationException",
					ex instanceof CommunicationException);
		}
	}

	@Test
	public void testNewOtherEndDisconnectedConnection() {
		try (Sockets sockets = new Sockets(6666)) {
			Socket server = sockets.getServer();
			Socket client = sockets.getClient();
			// Close other end
			server.close();
			ConnectionImpl.newConnection(client);
			fail("Creating a connection with an unconnected socket should fail");
		} catch (IOException | CommunicationException ex) {
			assertTrue("Creating a connection with an unconnected socket"
					+ " should throw a CommunicationException",
					ex instanceof CommunicationException);
		}
	}

	private static class MockMessage implements Message {
		private final long data;

		public MockMessage(long data) {
			this.data = data;
		}

		public long getData() {
			return data;
		}

		@Override
		public int hashCode() {
			int hash = 5;
			hash = 89 * hash + (int) (this.data ^ (this.data >>> 32));
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final MockMessage other = (MockMessage) obj;
			if (this.data != other.data) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return "MockMessage{" + "data=" + data + '}';
		}
	}

	private static class FailedMessage implements Message {
		private final Type type;

		public FailedMessage(Type type) {
			this.type = type;
		}

		private void writeObject(ObjectOutputStream out) throws IOException {
			type.onWrite();
			out.writeObject(type);
		}

		private void readObject(ObjectInputStream in) throws IOException,
															 ClassNotFoundException {
			((Type) in.readObject()).onRead();
		}

		public enum Type {
			SEND_EXCEPTION() {
				@Override
				public void onWrite() throws IOException {
					throw new IOException();
				}

				@Override
				public void onRead() {
				}
			},
			RECEIVE_EXCEPTION() {
				@Override
				public void onWrite() {
				}

				@Override
				public void onRead() throws IOException {
					throw new IOException();
				}
			},
			RECEIVE_CLASS_NOT_FOUND() {
				@Override
				public void onWrite() {
				}

				@Override
				public void onRead() throws ClassNotFoundException {
					throw new ClassNotFoundException();
				}
			};

			public abstract void onWrite() throws IOException;
			public abstract void onRead() throws IOException, ClassNotFoundException;
		}
	}

	private class Sockets implements Closeable {
		private final ServerSocket serverSocket;
		private final Socket server;
		private final Socket client;

		public Sockets(int port) throws IOException {
			serverSocket = new ServerSocket(port);
			try {
				client = new Socket(LOCALHOST, port);
				try {
					server = serverSocket.accept();
				} catch (IOException ex) {
					try {
						client.close();
					} finally {
						throw ex;
					}
				}
			} catch (IOException ex) {
				try {
				serverSocket.close();
				} finally {
					throw ex;
				}
			}
		}

		public Socket getServer() {
			return server;
		}

		public Socket getClient() {
			return client;
		}

		@Override
		public void close() throws IOException {
			try {
				client.close();
			} finally {
				try {
					server.close();
				} finally {
					serverSocket.close();
				}
			}
		}
	}

	private static class ConnectionCloser implements Runnable {
		private final Thread testThread;
		private final Closeable clientConnection;

		public ConnectionCloser(Thread testThread, Closeable clientConnection) {
			this.testThread = testThread;
			this.clientConnection = clientConnection;
		}

		@Override
		public void run() {
			try {
				// Try to give the other thread some time
				testThread.join(100);
			} catch (InterruptedException ex) {
				throw new AssertionError("Should never happen", ex);
			}
			finally {
				try {
					clientConnection.close();
				} catch (IOException ex) {
					throw new AssertionError("No recovery possible", ex);
				}
			}
		}
	}
}
