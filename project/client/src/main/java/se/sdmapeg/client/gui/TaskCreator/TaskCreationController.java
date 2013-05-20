package se.sdmapeg.client.gui.TaskCreator;

import se.sdmapeg.client.Client;
import se.sdmapeg.client.gui.ClientView;
import se.sdmapeg.common.tasks.Task;

public class TaskCreationController implements TaskCreationView.TaskCreationListener {
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
	public void taskFinnished(Task<?> task) {
		model.addTask(task);
	}

	public static TaskCreationController newTaskCreationController(Client model) {
		return new TaskCreationController(model);
	}
}
