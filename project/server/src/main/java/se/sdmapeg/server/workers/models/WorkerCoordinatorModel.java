package se.sdmapeg.server.workers.models;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sdmapeg.common.listeners.Listenable;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.common.tasks.SimpleFailure;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.server.workers.callbacks.WorkerCoordinatorCallback;
import se.sdmapeg.server.workers.callbacks.WorkerCoordinatorListener;
import se.sdmapeg.server.workers.callbacks.WorkerCoordinatorListenerSupport;
import se.sdmapeg.server.workers.exceptions.NoWorkersAvailableException;
import se.sdmapeg.server.workers.exceptions.TaskRejectedException;
import se.sdmapeg.server.workers.exceptions.WorkerRejectedException;
import se.sdmapeg.serverworker.TaskId;

/**
 * Class representing the internal state of a worker coordinator.
 */
public final class WorkerCoordinatorModel
		implements Listenable<WorkerCoordinatorListener> {
	private static final Logger LOG = LoggerFactory.getLogger(WorkerCoordinatorModel.class);
	private final WorkerCoordinatorListenerSupport listeners;
	private final WorkerCoordinatorCallback callback;
	private final Map<TaskId, Task<?>> taskMap =
		new ConcurrentHashMap<>();
	private final Map<TaskId, Worker> taskAssignmentMap =
		new ConcurrentHashMap<>();
	private final ConcurrentMap<InetSocketAddress, Worker> addressMap =
		new ConcurrentHashMap<>();

	/**
	 * Creates a new WorkerCoordinatorModel with the specified listener support
	 * and callback.
	 *
	 * @param listeners listener support to be used by this model
	 * @param callback callback to be notified of events
	 */
	public WorkerCoordinatorModel(WorkerCoordinatorListenerSupport listeners,
			WorkerCoordinatorCallback callback) {
		this.listeners = listeners;
		this.callback = callback;
	}

	/**
	 * Adds the specified worker to this model. The worker must have a unique
	 * address.
	 *
	 * @param worker the worker to be added
	 * @throws WorkerRejectedException if a worker with the same address was
	 *                                 already present
	 */
	public void addWorker(Worker worker) throws WorkerRejectedException {
		if (addressMap.putIfAbsent(worker.getAddress(), worker) == null) {
			LOG.info("{} connected", worker);
			listeners.workerConnected(worker.getAddress());
		} else {
			LOG.warn("Connection refused: {} attempted to connect, but was"
				+ " already connected", worker);
			throw new WorkerRejectedException();
		}
	}

	/**
	 * Returns the worker with the specified address, or {@code null} if no
	 * worker with said address was present.
	 *
	 * @param workerAddress the address of the worker
	 * @return the worker with the specified address, or {@code null} if no
	 *         worker with said address was present
	 */
	public Worker getWorker(InetSocketAddress workerAddress) {
		return addressMap.get(workerAddress);
	}

	/**
	 * Returns an unmodifiable snapshot of all workers currently present.
	 *
	 * @return an unmodifiable snapshot of all workers currently present
	 */
	public Set<Worker> getWorkers() {
		return Collections.unmodifiableSet(new HashSet<>(addressMap.values()));
	}

	/**
	 * Removes the specified worker from this model.
	 *
	 * @param worker the worker to remove
	 */
	public void removeWorker(Worker worker) {
		if (addressMap.remove(worker.getAddress()) == null) {
			return;
		}
		LOG.info("{} disconnected", worker);
		listeners.workerDisconnected(worker.getAddress());
		for (TaskId taskId : worker.getActiveTasks()) {
			LOG.info("Reassigning task {} from {}", taskId, worker);
			reassignTask(taskId);
		}
	}

	@Override
	public void addListener(WorkerCoordinatorListener listener) {
		listeners.addListener(listener);
	}

	@Override
	public void removeListener(WorkerCoordinatorListener listener) {
		listeners.removeListener(listener);
	}

	/**
	 * Handles the specified task by delegating it to an available worker.
	 *
	 * @param taskId the ID of the task to handle
	 * @param task the task to handle
	 */
	public void handleTask(TaskId taskId, Task<?> task) {
		taskMap.put(taskId, task);
		try {
			Worker worker = assignTask(taskId, task);
			LOG.info("Task {} assigned to {}", taskId, worker);
			listeners.taskAssigned(taskId, worker.getAddress());
		}
		catch (NoWorkersAvailableException ex) {
			callback.handleResult(taskId, SimpleFailure.newSimpleFailure(
					new ExecutionException("No workers available", null)));
		}
	}

	/**
	 * Completes the task with the specified ID.
	 *
	 * @param taskId the ID of the completed task
	 * @param result the result of the completed task
	 */
	public void completeTask(TaskId taskId, Result<?> result) {
		taskMap.remove(taskId);
		Worker worker = taskAssignmentMap.remove(taskId);
		if (worker != null) {
			LOG.info("Task {} completed by {}", taskId, worker);
			listeners.resultReceived(taskId, worker.getAddress());
			callback.handleResult(taskId, result);
		} else {
			LOG.warn("Attempted to complete task {}, which was not assigned" +
					 " to any worker", taskId);
		}
	}

	/**
	 * Cancels the task with the specified ID.
	 *
	 * @param taskId the ID of the task to cancel
	 */
	public void cancelTask(TaskId taskId) {
		taskMap.remove(taskId);
		Worker worker = taskAssignmentMap.remove(taskId);
		if (worker != null) {
			worker.cancelTask(taskId);
			LOG.info("Task {} cancelled", taskId);
			listeners.taskAborted(taskId, worker.getAddress());
		} else {
			LOG.warn("Attempted to cancel task {}, which was not assigned" +
					 " to any worker", taskId);
		}
	}

	private Worker assignTask(TaskId taskId, Task<?> task)
			throws NoWorkersAvailableException {
		while (true) {
			Worker worker = leastLoadedAvailableWorker();
			try {
				assignTask(taskId, task, worker);
				// Task successfully assigned
				return worker;
			} catch (TaskRejectedException ex) {
				// Task was rejected, keep trying
			}
		}
	}

	private void assignTask(TaskId taskId, Task<?> task, Worker worker)
			throws TaskRejectedException {
		taskAssignmentMap.put(taskId, worker);
		try {
			worker.assignTask(taskId, task);
		} catch (TaskRejectedException ex) {
			taskAssignmentMap.remove(taskId);
			throw ex;
		}
	}

	private Worker leastLoadedAvailableWorker()
			throws NoWorkersAvailableException {
		List<Worker> workers = new ArrayList<>(addressMap.values());
		int leastLoad = Integer.MAX_VALUE;
		Worker selected = null;
		for (Worker candidate : workers) {
			if (candidate.isAcceptingWork()) {
				int load = candidate.getLoad();
				if (leastLoad > load) {
					selected = candidate;
					leastLoad = load;
				}
			}
		}
		if (selected == null) {
			throw new NoWorkersAvailableException();
		}
		return selected;
	}

	/**
	 * Requests the desired number of tasks to be stolen from the queues of
	 * workers with heavier load and reassigned to workers with lighter load.
	 *
	 * @param desired the desired number of tasks to steal
	 */
	public void stealTasks(int desired) {
		List<WorkerLoadSnapshot> snapshots = createLoadSnapshotList();
		Collections.sort(snapshots, WorkerLoadSnapshot.descendingComparator());
		int stolen = 0;
		for (WorkerLoadSnapshot snapshot : snapshots) {
			Worker worker = snapshot.getWorker();
			stolen += stealTasks(worker, desired - stolen);
			if (stolen == desired) {
				break;
			}
		}
	}

	private int stealTasks(Worker worker, int desired) {
		int load = worker.getLoad();
		if (load <= 1) {
			return 0;
		}
		int tasksToSteal = Math.min(load - 1, desired);
		worker.stealTasks(tasksToSteal);
		return tasksToSteal;
	}

	/**
	 * Cancels the task with the specified TaskId from the worker currently
	 * performing it and attempts to assign it to a new worker.
	 *
	 * @param taskId the task to be reassigned
	 */
	public void reassignTask(TaskId taskId) {
		Task<?> task = taskMap.get(taskId);
		if (task != null) {
			LOG.info("Reassigning task {}", taskId);
			cancelTask(taskId);
			handleTask(taskId, task);
		} else {
			LOG.warn("Attempted to reassign unassigned task {}", taskId);
		}
	}

	private List<WorkerLoadSnapshot> createLoadSnapshotList() {
		/*
		 * Since new workers may be added at any time, we give the list a
		 * slightly larger initial capacity.
		 */
		List<WorkerLoadSnapshot> snapshots = new ArrayList<>(
			addressMap.size() + 5);
		for (Worker worker : addressMap.values()) {
			snapshots.add(WorkerLoadSnapshot.newSnapshot(worker));
		}
		return snapshots;
	}
}
