package se.sdmapeg.server;

/**
 * Interface for representing the actual server.
 */
public interface Server {

	/**
	 * Shuts down this Server.
	 */
	void shutDown();

	/**
	 * Starts this server.
	 */
	void start();
	
}
