package se.sdmapeg.client.gui.taskcreation;

import se.sdmapeg.client.gui.listeners.TaskCreationListener;
import se.sdmapeg.client.models.Client;
import se.sdmapeg.common.tasks.Task;

public class TaskCreationController implements TaskCreationListener {
	private final Client model;
	private final TaskCreationView view;

	private TaskCreationController(Client model) {
		this.model = model;
		this.view = new TaskCreationView(this);
		view.pack();
	}

	public TaskCreationView getView() {
		return view;
	}

	@Override
	public void taskFinished(Task<?> task) {
		model.addTask(task);
		view.dispose();
	}

	public static TaskCreationController newTaskCreationController(Client model) {
		return new TaskCreationController(model);
	}
}
