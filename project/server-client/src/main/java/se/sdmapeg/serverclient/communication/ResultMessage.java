package se.sdmapeg.serverclient.communication;

import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.serverclient.ClientTaskId;

/**
 * Implementation of ServerToClientMessage containing a result message.
 */
public interface ResultMessage extends ServerToClientMessage {
	@Override
	public <T> T accept(Handler<T> handler);

	/**
	 * Returns the ClientTaskId of this ResultMessage.
	 * @return the ClientTaskId of this ResultMessage
	 */
	public ClientTaskId getId();

	/**
	 * Returns the Result of this ResultMessage.
	 * @return the Result of this ResultMessage
	 */
	public Result<?> getResult();
}
