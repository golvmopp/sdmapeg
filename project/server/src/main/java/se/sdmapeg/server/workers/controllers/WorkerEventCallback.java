package se.sdmapeg.server.workers.controllers;

import se.sdmapeg.server.workers.models.Worker;
import se.sdmapeg.server.workers.models.WorkerCoordinatorModel;
import se.sdmapeg.server.workers.callbacks.WorkerCallback;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.serverworker.TaskId;

/**
 * A WorkerCallback that delegates its events to a WorkerCoordinatorModel.
 */
public final class WorkerEventCallback implements WorkerCallback {
	private final WorkerCoordinatorModel state;
	private final Worker worker;

	/**
	 * Creates a new WorkerEventCallback to update the specified model when a
	 * worker event occurs.
	 *
	 * @param state the model to update
	 * @param worker the worker to be used as the source for all events
	 */
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
