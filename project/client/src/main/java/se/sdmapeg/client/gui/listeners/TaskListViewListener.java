package se.sdmapeg.client.gui.listeners;

import se.sdmapeg.serverclient.ClientTaskId;

public interface TaskListViewListener {
	void addButtonPressed();
	void taskCreated(ClientTaskId clientTaskId);
	void taskSendButtonPressed(ClientTaskId clientTaskId);
}
