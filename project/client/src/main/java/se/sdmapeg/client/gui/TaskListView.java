package se.sdmapeg.client.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.LineBorder;

import org.jdesktop.swingx.JXHyperlink;

import se.sdmapeg.client.Client;
import se.sdmapeg.client.ClientListener;
import se.sdmapeg.client.gui.tasks.PythonTask.PythonEditor;
import se.sdmapeg.common.tasks.PythonTask;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.serverclient.ClientTaskId;

public class TaskListView extends JPanel implements TaskCreationCallback {
	private final Client client;
	private final JPanel taskListView;
	private final JLabel connectionInfoLabel;
	//private final JXHyperlink connectButton;

	private final Map<ClientTaskId, TaskPanel> taskPanels;
	
	public TaskListView(Client client){
		taskPanels = new HashMap<>();

		setPreferredSize(new Dimension(290, 500));
		this.client = client;

		client.addListener(new ClientListenerImpl());

		setLayout(new BorderLayout());
		JPanel proxyPanel = new JPanel();
		JPanel centerList = new JPanel(new BorderLayout());
		JLabel titleLabel = new JLabel("Tasks");
		add(titleLabel, BorderLayout.NORTH);
		add(centerList, BorderLayout.CENTER);
		
		taskListView = new JPanel(new GridLayout(0, 1));
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
				new TaskCreationView(TaskListView.this);
			}			
		}
		);
		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (ClientTaskId clientTaskId : taskPanels.keySet()) {
					if (taskPanels.get(clientTaskId).isChecked()) {
						removeTask(clientTaskId, taskPanels.get(clientTaskId));
					}
				}
			}
		});
		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (ClientTaskId clientTaskId : taskPanels.keySet()) {
					if (taskPanels.get(clientTaskId).isChecked()) {
						sendTask(clientTaskId);
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
		//connectButton = new JXHyperlink();
		//connectButton.setText("Connect");
		connectionBar.add(connectionInfoLabel, BorderLayout.WEST);
		//connectionBar.add(connectButton, BorderLayout.EAST);
	}

	/*@Override
	public void dispose(){
		client.shutDown();
		super.dispose();
	}*/

	private void addTask() {
		new PythonEditor(new PythonEditor.Callback() {
			@Override
			public void submit(String pythonScript) {
				client.addTask(PythonTask.newPythonTask(pythonScript));
			}
		});
	}

	private void sendTask(ClientTaskId clientTaskId) {
		client.sendTask(clientTaskId);

	}

	private void removeTask(ClientTaskId clientTaskId, JPanel panel) {
		cancelTask(clientTaskId);
		taskListView.remove(panel);
		revalidate();
	}

	private void cancelTask(ClientTaskId clientTaskId) {
		client.cancelTask(clientTaskId);
	}

	/*public void addTask(String typeName){
		taskListView.add(new TaskPanel(typeName));
		SwingUtilities.getRoot(taskListView).validate();
	}
	
	public void addTask(String typeName, String taskName){
		taskListView.add(new TaskPanel(typeName, taskName));
	}*/
		
	
	//TODO: Remove this when done. Duh.
	public static void main(String[] args){
		JPanel frame =  new TaskListView(null);
		JFrame main = new JFrame();
		main.add(frame);
		main.setVisible(true);
	}

	@Override
	public void addTask(Task task) {
		client.addTask(task);
	}

	private final class ClientListenerImpl implements ClientListener {

		@Override
		public void taskCreated(final ClientTaskId clientTaskId) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					TaskPanel taskPanel = new TaskPanel("PythonTaskView", new TaskPanel.Callback() {
						@Override
						public void sendTask(ClientTaskId clientTaskId) {
							TaskListView.this.sendTask(clientTaskId);
						}

						@Override
						public void cancelTask(ClientTaskId clientTaskId) {
							TaskListView.this.cancelTask(clientTaskId);
						}

						@Override
						public void showResult(ClientTaskId clientTaskId) {
							JOptionPane.showMessageDialog(null, client.getResult(clientTaskId));
						}

						@Override
						public void taskRemoved(ClientTaskId clientTaskId, JPanel panel) {
							removeTask(clientTaskId, panel);
						}
					}, clientTaskId);
					taskPanels.put(clientTaskId, taskPanel);
					taskListView.add(taskPanel);
					SwingUtilities.getRoot(taskListView).validate();
				}
			});
		}

		@Override
		public void taskSent(ClientTaskId clientTaskId) {
			taskPanels.get(clientTaskId).taskSent(clientTaskId);
		}

		@Override
		public void taskCancelled(ClientTaskId clientTaskId) {
			taskPanels.get(clientTaskId).taskCancelled(clientTaskId);
		}

		@Override
		public void resultReceived(ClientTaskId clientTaskId) {
			taskPanels.get(clientTaskId).resultReceived(clientTaskId);
		}
	}
}
