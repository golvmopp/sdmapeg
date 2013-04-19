package se.sdmapeg.server.clients;

import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.serverworker.TaskId;

/**
 *
 * @author niclas
 */
interface ClientCallback {

	void taskReceived(Client client, TaskId taskId,
					  Task<?> task);

	void taskCancelled(Client client, TaskId taskId);

	void clientDisconnected(Client client);
	
}
