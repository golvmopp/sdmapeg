package se.sdmapeg.server;

import se.sdmapeg.common.Task;
import se.sdmapeg.serverworker.TaskId;

import java.net.InetAddress;

public interface WorkerCoordinator {
	void handleTask(TaskId taskId, Task<?> task);

	void shutDown();

	void disconnectWorker(InetAddress workerAddress);
}
