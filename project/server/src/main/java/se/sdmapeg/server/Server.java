package se.sdmapeg.server;

import se.sdmapeg.common.listeners.Listenable;

/**
 * Interface for representing the actual server.
 */
public interface Server extends Listenable<ServerListener> {

	/**
	 * Shuts down this Server.
	 */
	void shutDown();

	/**
	 * Starts this server.
	 */
	void start();
	
}
