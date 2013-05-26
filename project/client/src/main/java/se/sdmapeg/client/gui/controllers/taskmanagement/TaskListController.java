package se.sdmapeg.client.gui.controllers.taskmanagement;

import se.sdmapeg.client.gui.listeners.TaskListViewListener;
import se.sdmapeg.client.gui.listeners.TaskListener;
import se.sdmapeg.client.gui.views.StatisticsView;
import se.sdmapeg.client.gui.views.taskmanagement.TaskListView;
import se.sdmapeg.client.models.Client;
import se.sdmapeg.client.gui.controllers.taskcreation.TaskCreationController;
import se.sdmapeg.serverclient.ClientTaskId;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class TaskListController implements TaskListViewListener {
	private final Client model;
	private final TaskListView view;
	private final Map<ClientTaskId, TaskController> controllerMap;

	private TaskListController(Client model, StatisticsView statistics) {
		this.model = model;
		this.view = new TaskListView(model, statistics);
		view.addListener(this);
		controllerMap = new HashMap<>();
	}

	public JPanel getView() {
		return view;
	}

	public static TaskListController newTaskListController(Client model, StatisticsView statistics) {
		return new TaskListController(model, statistics);
	}

	@Override
	public void addButtonPressed() {
		TaskCreationController.newTaskCreationController(model);
	}

	@Override
	public void taskSendButtonPressed(ClientTaskId clientTaskId) {
		controllerMap.get(clientTaskId).send();
	}

	@Override
	public void taskCreated(ClientTaskId clientTaskId) {
		TaskController task = TaskController
				.newTaskController(model, model.getTask(clientTaskId), clientTaskId,
				                   new TaskListener() {
					                   @Override
					                   public void showResultButtonPressed(ClientTaskId clientTaskId) {
						                   JOptionPane.showMessageDialog(null, model.getResult(clientTaskId));
					                   }

					                   @Override
					                   public void removed(ClientTaskId clientTaskId) {
						                   view.removeTask(clientTaskId);
					                   }
				                   }, (int) view.getPreferredSize().getWidth()-30);
		controllerMap.put(clientTaskId, task);
		view.addTask(clientTaskId, task.getView());
	}
}
