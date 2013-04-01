package se.sdmapeg.server;

import se.sdmapeg.common.Result;
import se.sdmapeg.serverworker.TaskId;

import java.net.InetAddress;

public interface ClientManager {
	void handleResult(TaskId taskId, Result<?> result);

	void shutDown();

	void disconnectClient(InetAddress clientAddress);
}
