package se.sdmapeg.client.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import se.sdmapeg.client.Client;
import se.sdmapeg.client.ClientImpl;
import se.sdmapeg.client.gui.tasks.PythonEditor;
import se.sdmapeg.common.tasks.PythonTask;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.serverclient.ClientTaskId;

/**
 * Class that handles the Client gui.
 */
public class ClientView extends JFrame implements ActionListener {
	private final Client client;

	private ClientView(Client client) {
		this.client = client;

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLayout(new GridLayout(1, 2));

		TaskListView taskList = new TaskListView(client);
		add(taskList);

		setVisible(true);
		pack();
	}

	public void showConnectionError() {
		JOptionPane.showMessageDialog(null, "Connection to server was lost.");
	}

	public void showResult(Result<?> result) {
		try {
			JOptionPane.showMessageDialog(this, result.get());
		} catch (ExecutionException e) {
			JOptionPane.showMessageDialog(this, "Something went wrong when running the task.");
		}
	}

	@Override
	public void dispose() {
		client.shutDown();
		super.dispose();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Task task = PythonTask.newPythonTask(((JTextArea) e.getSource()).getText());
		ClientTaskId id = client.addTask(task);
		client.sendTask(id);
	}

	public static ClientView newView(Client client) {
		return new ClientView(client);
	}
}
