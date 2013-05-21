package se.sdmapeg.client.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutionException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import se.sdmapeg.client.Client;
import se.sdmapeg.client.gui.TaskManager.TaskListView;
import se.sdmapeg.common.tasks.PythonTask;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.serverclient.ClientTaskId;

/**
 * Class that handles the Client gui.
 */
public class ClientView extends JFrame {
	private final Client client;

	private ClientView(Client client) {
		this.client = client;

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLayout(new GridLayout(1, 2));

		setVisible(true);
	}

	@Override
	public void dispose() {
		client.shutDown();
		super.dispose();
	}

	public static ClientView newView(Client client) {
		return new ClientView(client);
	}
}
