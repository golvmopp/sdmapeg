package se.sdmapeg.serverworker.communication;

import se.sdmapeg.serverworker.TaskId;

/**
 *
 * @author niclas
 */
public interface TaskCancellationMessage extends ServerToWorkerMessage {
	TaskId getTaskId();

	@Override
	<T> T accept(Handler<T> handler);
}
