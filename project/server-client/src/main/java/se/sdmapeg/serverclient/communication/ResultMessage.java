package se.sdmapeg.serverclient.communication;

import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.serverclient.ClientTaskId;

/**
 * Implementation of ServerToClientMessage containing a result message.
 */
public final class ResultMessage implements ServerToClientMessage {
	private final ClientTaskId id;
	private final Result<?> result;

	private ResultMessage(ClientTaskId id, Result<?> result) {
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
	public ClientTaskId getId() {
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
	public static ResultMessage newResultMessage(ClientTaskId id, Result<?> result) {
		return new ResultMessage(id, result);
	}
}
