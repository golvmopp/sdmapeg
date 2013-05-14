package se.sdmapeg.client.gui;

import java.awt.*;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import org.jdesktop.swingx.JXHyperlink;

import se.sdmapeg.client.Client;
import se.sdmapeg.client.ClientListener;
import se.sdmapeg.client.gui.tasks.PythonTask.PythonEditor;
import se.sdmapeg.common.tasks.PythonTask;
import se.sdmapeg.serverclient.ClientTaskId;

public class TaskListView extends JPanel {
	private final Client client;
	private final JPanel taskListView;
	private final JLabel connectionInfoLabel;
	private final JXHyperlink connectButton;
	
	public TaskListView(Client client){
		setPreferredSize(new Dimension(300, 500));
		//this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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

		buttonPanel.add(clearButton);
		buttonPanel.add(addButton);
		buttonPanel.add(sendButton);
		centerList.add(buttonPanel, BorderLayout.SOUTH);
		
		JPanel connectionBar = new JPanel(new BorderLayout());
		add(connectionBar, BorderLayout.SOUTH);
		connectionInfoLabel = new JLabel("Connected to ...");
		connectButton = new JXHyperlink(); 
		connectButton.setText("Connect");  
		connectionBar.add(connectionInfoLabel, BorderLayout.WEST);
		connectionBar.add(connectButton, BorderLayout.EAST);	
	}

	/*@Override
	public void dispose(){
		client.shutDown();
		super.dispose();
	}*/

	private void addTask() {
		PythonEditor.newPythonEditor(new PythonEditor.Callback() {
			@Override
			public void submit(String pythonScript) {
				client.addTask(PythonTask.newPythonTask(pythonScript));
			}
		});
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
		//JFrame frame =  new TaskListView(null);
		//frame.pack();
		//frame.setVisible(true);
	}

	private final class ClientListenerImpl implements ClientListener {

		@Override
		public void taskCreated(final ClientTaskId clientTaskId) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					TaskPanel taskPanel = new TaskPanel("PythonTask", new TaskPanel.Callback() {
						@Override
						public void sendTask(ClientTaskId clientTaskId) {
							client.sendTask(clientTaskId);
						}

						@Override
						public void cancelTask(ClientTaskId clientTaskId) {
							client.cancelTask(clientTaskId);
						}
					});
					taskListView.add(taskPanel);
					SwingUtilities.getRoot(taskListView).validate();
				}
			});
		}

		@Override
		public void taskSent(ClientTaskId clientTaskId) {
			//To change body of implemented methods use File | Settings | File Templates.
		}

		@Override
		public void taskCancelled(ClientTaskId clientTaskId) {
			//To change body of implemented methods use File | Settings | File Templates.
		}

		@Override
		public void resultReceived(ClientTaskId clientTaskId) {
			//To change body of implemented methods use File | Settings | File Templates.
		}
	}
}
