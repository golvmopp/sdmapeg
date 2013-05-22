package se.sdmapeg.client.gui.listeners;

import se.sdmapeg.serverclient.ClientTaskId;

public interface TaskListener {
	void showResultButtonPressed(ClientTaskId clientTaskId);
	void removed(ClientTaskId clientTaskId);
}
