package se.sdmapeg.client.gui.views.taskmanagement;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.LineBorder;

import se.sdmapeg.client.gui.listeners.TaskListViewListener;
import se.sdmapeg.client.gui.listeners.TaskListener;
import se.sdmapeg.client.gui.controllers.taskmanagement.TaskController;
import se.sdmapeg.client.gui.views.StatisticsView;
import se.sdmapeg.client.models.Client;
import se.sdmapeg.client.models.ClientListener;
import se.sdmapeg.serverclient.ClientTaskId;

public class TaskListView extends JPanel {
	private final Client client;
	private TaskListViewListener listener;

	private final JPanel taskListView;

	private final Map<ClientTaskId, TaskPanel> tasks;

	public TaskListView(Client client, StatisticsView statistics){
		tasks = new HashMap<>();

		setPreferredSize(new Dimension(500, 500));
		this.client = client;

		client.addListener(new ClientListenerImpl());

		setLayout(new BorderLayout());
		JPanel proxyPanel = new JPanel(); //For making a proper list in scrollpane.
		JPanel centerList = new JPanel(new BorderLayout());
		add(centerList, BorderLayout.CENTER);

		taskListView = new JPanel(new GridLayout(0, 1, 0, 2));
		JScrollPane taskList = new JScrollPane();
		proxyPanel.add(taskListView);
		taskList.setViewportView(proxyPanel);
		taskList.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		taskList.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
		centerList.add(taskList);

		JButton addPanel = new JButton("+ Create a new Task");
		addPanel.setPreferredSize(new Dimension(getPreferredSize().width-30, 60));
		addPanel.setFont(new Font(null, 0, 25));
		addPanel.setForeground(Color.DARK_GRAY);
		addPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
		addPanel.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				listener.addButtonPressed();
			}
		});
		taskListView.add(addPanel);

		JPanel buttonPanel = new JPanel(new GridLayout(1, 0));
		BottomButton clearButton = new BottomButton("Clear", true);
		BottomButton addButton = new BottomButton("Add");
		BottomButton sendButton = new BottomButton("Send");
		addButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				listener.addButtonPressed();
			}
		});
		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (ClientTaskId clientTaskId : tasks.keySet()) {
					if (tasks.get(clientTaskId).isChecked()) {
						removeTask(clientTaskId);
					}
				}
			}
		});
		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (ClientTaskId clientTaskId : tasks.keySet()) {
					if (tasks.get(clientTaskId).isChecked()) {
						listener.taskSendButtonPressed(clientTaskId);
					}
				}
			}
		});

		buttonPanel.add(clearButton);
		buttonPanel.add(addButton);
		buttonPanel.add(sendButton);

		JPanel buttonAndStatisticsPanel = new JPanel(new BorderLayout());
		buttonAndStatisticsPanel.add(statistics, BorderLayout.NORTH);
		buttonAndStatisticsPanel.add(buttonPanel, BorderLayout.SOUTH);

		centerList.add(buttonAndStatisticsPanel, BorderLayout.SOUTH);
	}

	public void addTask(final ClientTaskId clientTaskId, final TaskPanel task) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				tasks.put(clientTaskId, task);
				taskListView.add(task);
				SwingUtilities.getRoot(taskListView).validate();
			}
		});
	}

	public void removeTask(ClientTaskId clientTaskId) {
		taskListView.remove(tasks.get(clientTaskId));
		revalidate();
	}

	public void addListener(TaskListViewListener listener) {
		this.listener = listener;
	}

	private final class ClientListenerImpl implements ClientListener {
		@Override
		public void taskCreated(ClientTaskId clientTaskId) {
			listener.taskCreated(clientTaskId);
		}

		@Override
		public void taskSent(ClientTaskId clientTaskId) {
			tasks.get(clientTaskId).taskSent(clientTaskId);
		}

		@Override
		public void taskCancelled(ClientTaskId clientTaskId) {
			tasks.get(clientTaskId).taskCancelled(clientTaskId);
		}

		@Override
		public void resultReceived(ClientTaskId clientTaskId) {
			tasks.get(clientTaskId).resultReceived(clientTaskId);
		}
	}

}
