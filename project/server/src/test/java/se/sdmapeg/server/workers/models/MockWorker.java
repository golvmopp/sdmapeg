package se.sdmapeg.server.workers.models;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.server.workers.callbacks.WorkerCallback;
import se.sdmapeg.server.workers.exceptions.TaskRejectedException;
import se.sdmapeg.serverworker.TaskId;

/**
 *
 * @author niclas
 */
public final class MockWorker implements Worker {
	private final Set<TaskId> activeTasks = new HashSet<>();
	private final InetSocketAddress address;
	private int parallellWorkCapacity = 1;
	private boolean connected = true;

	public MockWorker(InetSocketAddress address) {
		this.address = address;
	}

	@Override
	public InetSocketAddress getAddress() {
		return address;
	}

	@Override
	public void assignTask(TaskId taskId, Task<?> task)
			throws TaskRejectedException {
		if (connected) {
			activeTasks.add(taskId);
		}
		else {
			throw new TaskRejectedException();
		}
	}

	@Override
	public void cancelTask(TaskId taskId) {
		activeTasks.remove(taskId);
	}

	@Override
	public Set<TaskId> getActiveTasks() {
		return new HashSet<>(activeTasks);
	}

	@Override
	public int getLoad() {
		return activeTasks.size() - parallellWorkCapacity;
	}

	@Override
	public void stealTasks(int max) {
	}

	@Override
	public void listen(WorkerCallback callback) {
		callback.workerDisconnected();
	}

	@Override
	public void disconnect() {
		connected = false;
	}

	@Override
	public int getParallellWorkCapacity() {
		return parallellWorkCapacity;
	}

	public void setParallellWorkCapacity(int parallellWorkCapacity) {
		this.parallellWorkCapacity = parallellWorkCapacity;
	}

	@Override
	public boolean isAcceptingWork() {
		return connected;
	}
}
