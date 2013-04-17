package se.sdmapeg.client;

import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.serverclient.ClientTaskId;

/**
 * Interface for representing the actual client.
 */
public interface Client {
	public ClientTaskId addTask(Task task);
	public void sendTask(ClientTaskId id);
	public void start();
	public void shutDown();
}
