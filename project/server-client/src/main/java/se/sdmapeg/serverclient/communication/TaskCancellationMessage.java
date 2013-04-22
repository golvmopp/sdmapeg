package se.sdmapeg.serverclient.communication;

import se.sdmapeg.serverclient.ClientTaskId;

/**
 *
 * @author niclas
 */
public interface TaskCancellationMessage extends ClientToServerMessage {
	public ClientTaskId getTaskId();

	@Override
	public <T> T accept(Handler<T> handler);
}
