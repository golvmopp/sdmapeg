package se.sdmapeg.client;

import se.sdmapeg.serverclient.ClientTaskId;

public interface ClientListener {
	void taskCreated(ClientTaskId clientTaskId);
	void taskSent(ClientTaskId clientTaskId);
	void taskCancelled(ClientTaskId clientTaskId);
	void resultReceived(ClientTaskId clientTaskId);
}
