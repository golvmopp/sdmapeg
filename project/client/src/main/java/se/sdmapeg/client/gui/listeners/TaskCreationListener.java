package se.sdmapeg.client.gui.listeners;

import se.sdmapeg.common.tasks.Task;

public interface TaskCreationListener {
	void taskFinished(Task<?> task);
}
