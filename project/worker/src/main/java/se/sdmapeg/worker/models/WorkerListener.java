package se.sdmapeg.worker.models;

import se.sdmapeg.serverworker.TaskId;

public interface WorkerListener {
	void taskAdded(TaskId taskId);
	void taskStarted(TaskId taskId);
	void taskFinished(TaskId taskId);
	void taskCancelled(TaskId taskId);
	void taskStolen(TaskId taskId);
}
