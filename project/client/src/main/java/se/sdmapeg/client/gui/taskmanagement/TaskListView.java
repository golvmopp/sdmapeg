package se.sdmapeg.client.gui.taskmanagement;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.LineBorder;

import se.sdmapeg.client.gui.listeners.TaskListViewListener;
import se.sdmapeg.client.gui.listeners.TaskListener;
import se.sdmapeg.client.models.Client;
import se.sdmapeg.client.models.ClientListener;
import se.sdmapeg.serverclient.ClientTaskId;

public class TaskListView extends JPanel {
	private final Client client;
	private TaskListViewListener listener;

	private final JPanel taskListView;
	private final JLabel connectionInfoLabel;

	private final Map<ClientTaskId, TaskController> tasks;
	
	public TaskListView(Client client){
		tasks = new HashMap<>();

		setPreferredSize(new Dimension(290, 500));
		this.client = client;

		client.addListener(new ClientListenerImpl());

		setLayout(new BorderLayout());
		JPanel proxyPanel = new JPanel(); //For making a proper list in scrollpane. 
		JPanel centerList = new JPanel(new BorderLayout());
		JLabel titleLabel = new JLabel("Tasks");
		add(titleLabel, BorderLayout.NORTH);
		add(centerList, BorderLayout.CENTER);
		
		taskListView = new JPanel(new GridLayout(0, 1, 0, 2));
		JScrollPane taskList = new JScrollPane();
		proxyPanel.add(taskListView);
		taskList.setViewportView(proxyPanel);
		taskList.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		taskList.setBorder(new LineBorder(Color.BLACK));
		centerList.add(taskList);
		
		JPanel buttonPanel = new JPanel(new GridLayout(1, 0));
		BottomButton clearButton = new BottomButton("Clear", true);
		BottomButton addButton = new BottomButton("Add");
		BottomButton sendButton = new BottomButton("Send");
		addButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				listener.addButtonPressed();
			}			
		}
		);
		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (ClientTaskId clientTaskId : tasks.keySet()) {
					if (tasks.get(clientTaskId).isSelected()) {
						removeTask(clientTaskId);
					}
				}
			}
		});
		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (ClientTaskId clientTaskId : tasks.keySet()) {
					if (tasks.get(clientTaskId).isSelected()) {
						tasks.get(clientTaskId).send();
					}
				}
			}
		});

		buttonPanel.add(clearButton);
		buttonPanel.add(addButton);
		buttonPanel.add(sendButton);
		centerList.add(buttonPanel, BorderLayout.SOUTH);
		
		JPanel connectionBar = new JPanel(new BorderLayout());
		add(connectionBar, BorderLayout.SOUTH);
		connectionInfoLabel = new JLabel("Connected to: " + client.getHost());
		connectionBar.add(connectionInfoLabel, BorderLayout.WEST);
	}

	private void removeTask(ClientTaskId clientTaskId) {
		taskListView.remove(tasks.get(clientTaskId).getView());
		revalidate();
	}

	public void addListener(TaskListViewListener listener) {
		this.listener = listener;
	}

	private final class ClientListenerImpl implements ClientListener {
		@Override
		public void taskCreated(final ClientTaskId clientTaskId) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					TaskController task = TaskController
							.newTaskController(client, client.getTask(clientTaskId), clientTaskId,
							                   new TaskListener() {
						@Override
						public void showResultButtonPressed(ClientTaskId clientTaskId) {
							JOptionPane.showMessageDialog(null, client.getResult(clientTaskId));
						}

						@Override
						public void removed(ClientTaskId clientTaskId) {
							removeTask(clientTaskId);
						}
					});
					tasks.put(clientTaskId, task);
					taskListView.add(task.getView());
					SwingUtilities.getRoot(taskListView).validate();
				}
			});
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
