package se.sdmapeg.server.communication;

import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sdmapeg.common.communication.CommunicationException;
import se.sdmapeg.common.communication.Message;

/**
 * Class for accepting connections from a ConnectionHandler.
 */
public final class ConnectionAcceptor<S extends Message, R extends Message> {
	private static final Logger LOG = LoggerFactory.getLogger(ConnectionAcceptor.class);
	private final ConnectionHandler<S, R> connectionHandler;
	private final ConnectionAcceptorCallback<S, R> callback;

	private ConnectionAcceptor(ConnectionHandler<S, R> connectionHandler,
			ConnectionAcceptorCallback<S, R> callback) {
		this.connectionHandler = connectionHandler;
		this.callback = callback;
	}

	private void acceptConnections() {
		try {
			while (true) {
				callback.connectionReceived(connectionHandler.accept());
			}
		} catch (SocketException ex) {
			LOG.info("Connecting handler closed.");
		} catch (CommunicationException ex) {
			LOG.error("An error occurred while listening for connections",
					  ex);
		} finally {
			callback.connectionHandlerClosed();
		}
	}

	/**
	 * Starts a task to continually accept new connections.
	 *
	 * @param <S> the type of messages which can be sent over the accepted
	 *            connections
	 * @param <R> the type of messages which can be received over the accepted
	 *            connections
	 * @param threadPool the thread pool to run the task listening for new
	 *                   connections
	 * @param connectionHandler the connection handler to accept new connections
	 *                          from
	 * @param callback the callback to notify of new connections
	 */
	public static <S extends Message, R extends Message> void acceptConnections(
			ExecutorService threadPool,
			ConnectionHandler<S, R> connectionHandler,
			ConnectionAcceptorCallback<S, R> callback) {
		final ConnectionAcceptor<S, R> connectionAcceptor =
			new ConnectionAcceptor<>(connectionHandler, callback);
		threadPool.submit(new Runnable() {
			@Override
			public void run() {
				connectionAcceptor.acceptConnections();
			}
		});
	}
}
