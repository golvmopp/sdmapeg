package se.sdmapeg.server.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import se.sdmapeg.common.communication.CommunicationException;
import se.sdmapeg.common.communication.Connection;
import se.sdmapeg.common.communication.ConnectionClosedException;
import se.sdmapeg.common.communication.Message;

/**
 *
 * @author niclas
 */
public final class MockConnection<S extends Message, R extends Message> implements Connection<S, R> {
	private final InetSocketAddress address;
	private final R closePoision;
	private final Map<R, CommunicationException> receivePoison =
												 new ConcurrentHashMap<>();
	private final BlockingDeque<R> input =
								   new LinkedBlockingDeque<>();
	private final BlockingQueue<S> output =
								   new LinkedBlockingQueue<>();
	private final BlockingQueue<CommunicationException> sendPoison =
														new LinkedBlockingQueue<>();
	private volatile boolean exceptionOnClose = false;
	private volatile boolean closed = false;

	public MockConnection(InetSocketAddress address, R closePoision) {
		this.address = address;
		this.closePoision = closePoision;
	}

	@Override
	public InetSocketAddress getAddress() {
		return address;
	}

	@Override
	public void send(S message) throws CommunicationException,
									   ConnectionClosedException {
		if (!isOpen()) {
			throw new ConnectionClosedException();
		}
		checkSendPoison();
		output.add(message);
	}

	@Override
	public R receive() throws CommunicationException, ConnectionClosedException {
		if (!isOpen()) {
			throw new ConnectionClosedException();
		}
		try {
			R message = input.take();
			checkReceivePoison(message);
			return message;
		}
		catch (InterruptedException ex) {
			throw new ConnectionClosedException();
		}
	}

	@Override
	public boolean isOpen() {
		return !closed;
	}

	@Override
	public void close() throws IOException {
		closed = true;
		receivePoison.put(closePoision, new ConnectionClosedException());
		input.push(closePoision);
		if (exceptionOnClose) {
			throw new IOException();
		}
	}

	public void addReceived(R message) {
		input.add(message);
	}

	public void addReceiveException(R poison, CommunicationException exeption) {
		receivePoison.put(poison, exeption);
		addReceived(poison);
	}

	public void addReceiveDisconnection() {
		addReceiveException(closePoision, new ConnectionClosedException());
	}

	public void addSendException(CommunicationException exception) {
		sendPoison.add(exception);
	}

	public void addSendDisconnection() {
		addSendException(new ConnectionClosedException());
	}

	public S getSent() {
		return output.poll();
	}

	public void setExceptionOnClose(boolean fail) {
		this.exceptionOnClose = fail;
	}

	private void checkReceivePoison(R message) throws CommunicationException {
		CommunicationException exception = receivePoison.remove(message);
		if (exception != null) {
			throw exception;
		}
	}

	private void checkSendPoison() throws CommunicationException {
		CommunicationException exception = sendPoison.poll();
		if (exception != null) {
			throw exception;
		}
	}
	
}
