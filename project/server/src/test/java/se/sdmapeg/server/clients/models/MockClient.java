package se.sdmapeg.server.clients.models;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.server.clients.callbacks.ClientCallback;
import se.sdmapeg.serverworker.TaskId;

/**
 *
 * @author niclas
 */
public final class MockClient implements Client {
	private final InetSocketAddress address;
	private final Set<TaskId> activeTasks =
							  new HashSet<>();

	public MockClient(InetSocketAddress address) {
		this.address = address;
	}

	@Override
	public void disconnect() {
	}

	@Override
	public Set<TaskId> getActiveTasks() {
		return Collections.unmodifiableSet(new HashSet<>(activeTasks));
	}

	@Override
	public InetSocketAddress getAddress() {
		return address;
	}

	@Override
	public void listen(ClientCallback callback) {
		callback.clientDisconnected();
	}

	@Override
	public void taskCompleted(TaskId taskId,
							  Result<?> result) {
		removeTask(taskId);
	}

	public void addTask(TaskId taskId) {
		activeTasks.add(taskId);
	}

	public void removeTask(TaskId taskId) {
		activeTasks.remove(taskId);
	}
	
}
