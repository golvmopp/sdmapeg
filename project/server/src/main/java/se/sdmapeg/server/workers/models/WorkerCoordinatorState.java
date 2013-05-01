package se.sdmapeg.server.workers.models;

/**
 * Enum representing the state of a worker coordinator.
 */
public enum WorkerCoordinatorState {
	/**
	 * Indicates that a worker coordinator has been created but not yet
	 * started.
	 */
	CREATED,
	/**
	 * Indicates that a worker coordinator has been created and started.
	 */
	STARTED,
	/**
	 * Indicates that a worker coordinator has been stopped.
	 */
	STOPPED;
}
