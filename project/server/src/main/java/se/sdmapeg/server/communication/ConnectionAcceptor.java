package se.sdmapeg.server.communication;

import java.net.SocketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sdmapeg.common.communication.CommunicationException;
import se.sdmapeg.common.communication.Connection;
import se.sdmapeg.common.communication.Message;

/**
 *
 * @author niclas
 */
public class ConnectionAcceptor<S extends Message, R extends Message>
		implements Runnable{
	private static final Logger LOG = LoggerFactory.getLogger(ConnectionAcceptor.class);
	private final ConnectionHandler<S, R> connectionHandler;
	private final Callback<S, R> callback;

	public ConnectionAcceptor(ConnectionHandler<S, R> connectionHandler,
							  Callback<S, R> callback) {
		this.connectionHandler = connectionHandler;
		this.callback = callback;
	}

	@Override
	public void run() {
		try {
			while (true) {
				callback.connectionReceived(connectionHandler.accept());
			}
		} catch (SocketException ex) {
			// The connection handler was shut down.
		} catch (CommunicationException ex) {
			LOG.error("An error occurred while listening for connections",
					  ex);
		} finally {
			callback.connectionHandlerClosed();
		}
	}

	public interface Callback<S extends Message, R extends Message> {
		void connectionReceived(Connection<S, R> connection);
		void connectionHandlerClosed();
	}
}
