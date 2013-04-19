package se.sdmapeg.serverworker.communication;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import se.sdmapeg.serverworker.TaskId;

public class WorkStealingResponse implements WorkerToServerMessage {
	private static final long serialVersionUID = 1594015099229454379L;
	private final Set<TaskId> stolenIds;

	private WorkStealingResponse(Set<TaskId> stolenIds) {
		this.stolenIds = new HashSet<>(stolenIds);
	}

	@Override
	public <T> T accept(Handler<T> handler) {
		return handler.handle(this);
	}

	public Set<TaskId> getStolenTasks() {
		return Collections.unmodifiableSet(stolenIds);
	}

	public WorkStealingResponse newWorkStealingResponse(Set<TaskId> stolenIds) {
		return new WorkStealingResponse(stolenIds);
	}
}
