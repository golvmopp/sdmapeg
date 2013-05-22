package se.sdmapeg.client.gui;

import se.sdmapeg.client.models.Client;
import se.sdmapeg.client.gui.taskmanagement.TaskListController;

import javax.swing.*;

public class ClientController {
	private final Client model;
	private final JFrame view;

	private ClientController(Client model) {
		this.model = model;
		this.view = ClientView.newView(model);
		model.start();

		TaskListController taskList = TaskListController.newTaskListController(model);
		view.add(taskList.getView());
		view.pack();
	}

	public JFrame getView() {
		return view;
	}

	public static ClientController newClientController(Client model) {
		return new ClientController(model);
	}
}
