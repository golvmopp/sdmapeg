package se.sdmapeg.server.clients.models.test;

import java.util.concurrent.Callable;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.server.clients.callbacks.ClientCallback;
import se.sdmapeg.server.clients.models.Client;
import se.sdmapeg.serverworker.TaskId;

/**
 *
 * @author niclas
 */
public class ClientCallbackListener implements Callable<Throwable> {
	private final Client instance;

	public ClientCallbackListener(Client instance) {
		this.instance = instance;
	}

	@Override
	public Throwable call() throws Exception {
		try {
			instance.listen(new ClientCallback() {
				@Override
				public void taskReceived(TaskId taskId,
										 Task<?> task) {
				}

				@Override
				public void taskCancelled(TaskId taskId) {
				}

				@Override
				public void clientDisconnected() {
				}
			});
		}
		catch (Throwable ex) {
			return ex;
		}
		return null;
	}
	
}
