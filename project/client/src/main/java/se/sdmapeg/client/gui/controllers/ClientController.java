package se.sdmapeg.client.gui.controllers;

import se.sdmapeg.client.gui.views.ClientView;
import se.sdmapeg.client.gui.views.StatisticsView;
import se.sdmapeg.client.models.Client;
import se.sdmapeg.client.gui.controllers.taskmanagement.TaskListController;

import javax.swing.*;
import java.awt.*;

public class ClientController {
	private final Client model;
	private final JFrame view;

	private ClientController(Client model, String host) {
		this.model = model;
		this.view = ClientView.newView(model);
		model.start();

		StatisticsView statisticsView = new StatisticsView(host);
		model.addListener(statisticsView);
		view.add(statisticsView, BorderLayout.WEST);

		TaskListController taskList = TaskListController.newTaskListController(model);
		view.add(taskList.getView(), BorderLayout.EAST);

		view.pack();
	}

	public JFrame getView() {
		return view;
	}

	public static ClientController newClientController(Client model, String host) {
		return new ClientController(model, host);
	}
}
