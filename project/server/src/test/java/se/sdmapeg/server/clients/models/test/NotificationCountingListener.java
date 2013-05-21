package se.sdmapeg.server.clients.models.test;

import java.net.InetSocketAddress;
import se.sdmapeg.server.clients.callbacks.ClientManagerListener;
import se.sdmapeg.serverworker.TaskId;

/**
 *
 * @author niclas
 */
public final class NotificationCountingListener implements ClientManagerListener {
	private int notifications = 0;

	public int getNotifications() {
		return notifications;
	}

	@Override
	public void clientConnected(InetSocketAddress address) {
		notifications++;
	}

	@Override
	public void clientDisconnected(InetSocketAddress address) {
		notifications++;
	}

	@Override
	public void taskReceived(TaskId taskId, InetSocketAddress address) {
		notifications++;
	}

	@Override
	public void taskCancelled(TaskId taskId, InetSocketAddress address) {
		notifications++;
	}

	@Override
	public void resultSent(TaskId taskId, InetSocketAddress address) {
		notifications++;
	}
	
}
