package se.sdmapeg.serverworker.communication;

import java.util.Set;

import se.sdmapeg.serverworker.TaskId;

public interface WorkStealingResponseMessage extends WorkerToServerMessage {
	@Override
	<T> T accept(Handler<T> handler);

	Set<TaskId> getStolenTasks();
}
