package se.sdmapeg.serverclient.communication;

import se.sdmapeg.serverclient.ClientTaskId;

/**
 *
 * @author niclas
 */
public interface TaskCancellationMessage extends ClientToServerMessage {
	ClientTaskId getTaskId();

	@Override
	<T> T accept(Handler<T> handler);
}
