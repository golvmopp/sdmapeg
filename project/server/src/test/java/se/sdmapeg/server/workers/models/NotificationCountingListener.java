package se.sdmapeg.server.workers.models;

import java.net.InetSocketAddress;
import se.sdmapeg.server.workers.callbacks.WorkerCoordinatorListener;
import se.sdmapeg.serverworker.TaskId;

/**
 *
 * @author niclas
 */
public final class NotificationCountingListener implements WorkerCoordinatorListener {
	private int notifications = 0;

	public int getNotifications() {
		return notifications;
	}

	@Override
	public void workerConnected(InetSocketAddress address) {
		notifications++;
	}

	@Override
	public void workerDisconnected(InetSocketAddress address) {
		notifications++;
	}

	@Override
	public void resultReceived(TaskId taskId, InetSocketAddress address) {
		notifications++;
	}

	@Override
	public void taskAssigned(TaskId taskId, InetSocketAddress address) {
		notifications++;
	}

	@Override
	public void taskAborted(TaskId taskId, InetSocketAddress address) {
		notifications++;
	}
}
