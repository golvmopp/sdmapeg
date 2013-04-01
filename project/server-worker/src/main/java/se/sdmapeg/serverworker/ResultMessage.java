package se.sdmapeg.serverworker;

import se.sdmapeg.common.tasks.Result;

public final class ResultMessage implements WorkerToServerMessage {
    	private static final long serialVersionUID = 1L;
	private final TaskId id;
	private final Result<?> result;

	private ResultMessage(TaskId id, Result<?> result) {
		this.id = id;
		this.result = result;
	}

	@Override
	public <T> T accept(Visitor<T> visitor) {
		return visitor.visit(this);
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
	public static ResultMessage newResultMessage(TaskId id, Result<?> result) {
		return new ResultMessage(id, result);
	}

}
