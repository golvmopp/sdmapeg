package se.sdmapeg.client;

import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.serverclient.ClientTaskId;

/**
 * Interface for representing the actual client.
 */
public interface Client {
	ClientTaskId addTask(Task task);

	void sendTask(ClientTaskId clientTaskId);

	void start();

	void abortTask(ClientTaskId clientTaskId);

	void shutDown();
}
