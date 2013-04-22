package se.sdmapeg.serverworker.communication;

import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.serverworker.TaskId;

public final class ResultMessage implements WorkerToServerMessage {
    	private static final long serialVersionUID = 1L;
	private final TaskId id;
	private final Result<?> result;

	private ResultMessage(TaskId id, Result<?> result) {
		this.id = id;
		this.result = result;
	}

	@Override
	public <T> T accept(Handler<T> handler) {
		return handler.handle(this);
	}

	/**
	 * Returns the ClientTaskId of this ResultMessage.
	 * @return the ClientTaskId of this ResultMessage
	 */
	public TaskId getId() {
		return id;
	}

	/**
	 * Returns the Result of this ResultMessage.
	 * @return the Result of this ResultMessage
	 */
	public Result<?> getResult() {
		return result;
	}

	/**
	 * Returns a new ResultMessage.
	 * @param id The ClientTaskId linked with the result
	 * @param result The Result to be sent back to the client
	 * @return the new ResultMessage
	 */
	public static WorkerToServerMessage newResultMessage(TaskId id, Result<?> result) {
		return new ResultMessage(id, result);
	}

}
