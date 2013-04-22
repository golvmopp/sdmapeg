package se.sdmapeg.client;

import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.serverclient.ClientTaskId;


/**
 * A callback to be notified by a Server when an event occurs.
 */
public interface ServerCallback {

	/**
	 * Notifies this callback that the result for the task with the specified
	 * taskId has been received.
	 *
	 * @param taskId the client-side ID of the completed task
	 * @param result the result of the completed task
	 */
	void resultReceived(ClientTaskId taskId, Result<?> result);

	/**
	 * Notifies this callback that the connection to the server has been closed.
	 */
	void connectionClosed();
}
