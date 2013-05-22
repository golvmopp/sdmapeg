package se.sdmapeg.client.gui.taskmanagement;

import se.sdmapeg.client.gui.listeners.TaskListViewListener;
import se.sdmapeg.client.models.Client;
import se.sdmapeg.client.gui.taskcreation.TaskCreationController;

import javax.swing.*;

public class TaskListController implements TaskListViewListener {
	private final Client model;
	private final TaskListView view;

	private TaskListController(Client model) {
		this.model = model;
		this.view = new TaskListView(model);
		view.addListener(this);
	}

	public JPanel getView() {
		return view;
	}

	public static TaskListController newTaskListController(Client model) {
		return new TaskListController(model);
	}

	@Override
	public void addButtonPressed() {
		TaskCreationController.newTaskCreationController(model);
	}
}
