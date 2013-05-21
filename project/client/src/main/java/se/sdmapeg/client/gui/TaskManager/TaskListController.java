package se.sdmapeg.client.gui.TaskManager;

import se.sdmapeg.client.Client;
import se.sdmapeg.client.gui.TaskCreator.TaskCreationController;
import se.sdmapeg.client.gui.TaskCreator.TaskCreationView;

import javax.swing.*;

public class TaskListController implements TaskListView.TaskListViewListener {
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
