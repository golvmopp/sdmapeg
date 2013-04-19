package se.sdmapeg.server.clients;

/**
 * Enum representing the state of a client manager.
 */
public enum ClientManagerState {
	/**
	 * Indicates that a client manager has been created but not yet started.
	 */
	CREATED,
	/**
	 * Indicates that a client manager has been created and started.
	 */
	STARTED,
	/**
	 * Indicates that a client manager has been stopped.
	 */
	STOPPED;
}
