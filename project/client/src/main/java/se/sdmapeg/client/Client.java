package se.sdmapeg.client;

import se.sdmapeg.common.listeners.Listenable;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.serverclient.ClientTaskId;

/**
 * Interface for representing the actual client.
 */
public interface Client extends Listenable<ClientListener> {
	ClientTaskId addTask(Task<?> task);

	void sendTask(ClientTaskId clientTaskId);

	void start();

	void cancelTask(ClientTaskId clientTaskId);

	void shutDown();
}
