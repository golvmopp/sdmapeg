package se.sdmapeg.serverworker.communication;

import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.serverworker.TaskId;

public interface ResultMessage extends WorkerToServerMessage {
	@Override
	<T> T accept(Handler<T> handler);

	/**
	 * Returns the ClientTaskId of this ResultMessage.
	 * @return the ClientTaskId of this ResultMessage
	 */
	TaskId getId();

	/**
	 * Returns the Result of this ResultMessage.
	 * @return the Result of this ResultMessage
	 */
	Result<?> getResult();

}
