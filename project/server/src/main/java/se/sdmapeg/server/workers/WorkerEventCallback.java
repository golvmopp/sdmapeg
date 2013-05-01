package se.sdmapeg.server.workers;

import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.serverworker.TaskId;

/**
 * A WorkerCallback that delegates its events to a WorkerCoordinatorModel.
 */
final class WorkerEventCallback implements WorkerCallback {
	private final WorkerCoordinatorModel state;
	private final Worker worker;

	public WorkerEventCallback(WorkerCoordinatorModel state, Worker worker) {
		this.state = state;
		this.worker = worker;
	}

	@Override
	public void taskCompleted(TaskId taskId, Result<?> result) {
		state.completeTask(taskId, result);
	}

	@Override
	public void taskStolen(TaskId taskId) {
		state.reassignTask(taskId);
	}

	@Override
	public void workerDisconnected() {
		state.removeWorker(worker);
	}

	@Override
	public void workRequested() {
		state.stealTasks(worker.getParallellWorkCapacity());
	}
}
