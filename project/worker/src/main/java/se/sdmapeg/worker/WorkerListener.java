package se.sdmapeg.worker;

import se.sdmapeg.serverworker.TaskId;

public interface WorkerListener {
	void taskAdded(TaskId taskId);
	void taskStarted(TaskId taskId);
	void taskFinished(TaskId taskId);
}
