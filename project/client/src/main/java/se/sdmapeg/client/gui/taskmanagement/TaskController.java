package se.sdmapeg.client.gui.taskmanagement;

import se.sdmapeg.client.gui.listeners.TaskListener;
import se.sdmapeg.client.gui.listeners.TaskPanelListener;
import se.sdmapeg.client.models.Client;
import se.sdmapeg.client.models.ClientListener;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.serverclient.ClientTaskId;

public class TaskController implements TaskPanelListener, ClientListener {
	private final Client client;
	private final TaskModel model;
	private final TaskPanel view;
	private final TaskListener listener;

	private TaskController(Client client, Task<?> task, ClientTaskId clientTaskId, TaskListener listener) {
		this.client = client;
		this.model = TaskModel.newTaskModel(task, clientTaskId);
		this.view = new TaskPanel(model, this);
		this.listener = listener;
	}

	public TaskPanel getView() {
		return view;
	}

	public boolean isSelected() {
		return view.isChecked();
	}

	public void send() {
		if (model.getState() == TaskModel.TaskState.CREATED) {
			client.sendTask(model.getClientTaskId());
		}
	}

	public void cancel() {
		client.cancelTask(model.getClientTaskId());
	}

	public static TaskController newTaskController(Client client, Task<?> task, ClientTaskId clientTaskId, TaskListener listener) {
		return new TaskController(client, task, clientTaskId, listener);
	}

	@Override
	public void taskCreated(ClientTaskId clientTaskId) {
		view.taskCreated(clientTaskId);
	}

	@Override
	public void taskSent(ClientTaskId clientTaskId) {
		model.setState(TaskModel.TaskState.SENT);
		model.setStartTime(System.currentTimeMillis());
		view.taskSent(clientTaskId);
	}

	@Override
	public void taskCancelled(ClientTaskId clientTaskId) {
		model.setState(TaskModel.TaskState.FAILED);
		view.taskCancelled(clientTaskId);
	}

	@Override
	public void resultReceived(ClientTaskId clientTaskId) {
		model.setState(TaskModel.TaskState.COMPLETED);
		view.resultReceived(clientTaskId);
	}

	@Override
	public void sendButtonPressed() {
		send();
	}

	@Override
	public void cancelButtonPressed() {
		cancel();
	}

	@Override
	public void showResultButtonPressed() {
		listener.showResultButtonPressed(model.getClientTaskId());
	}

	@Override
	public void removeButtonPressed() {
		if (model.getState() == TaskModel.TaskState.SENT) {
			cancel();
		}
		listener.removed(model.getClientTaskId());
	}
}
