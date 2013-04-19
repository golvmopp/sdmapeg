package se.sdmapeg.server;

import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sdmapeg.common.communication.CommunicationException;

/**
 *
 * @author niclas
 */
public final class Main {
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	private Main() {
		// Prevent instantiation
		throw new AssertionError();
	}

	public static void main(String[] args) {
		Server server;
		try {
			server = new ServerImpl();
		} catch (CommunicationException ex) {
			LOG.error("Failed to create server", ex);
			return;
		}
		server.start();
		System.out.println("Server started and running. Type \"exit\" to close.");
		Scanner scanner = new Scanner(System.in);
		while (!scanner.nextLine().equals("exit")) {
			// Do nothing
		}
		server.shutDown();
	}
}
