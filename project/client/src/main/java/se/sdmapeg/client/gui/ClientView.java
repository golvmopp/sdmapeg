package se.sdmapeg.client.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import se.sdmapeg.client.ClientImpl;
import se.sdmapeg.common.tasks.PythonTask;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.serverclient.ClientTaskId;

/**
 * Class that handles the Client gui.
 */
public class ClientView implements ActionListener {
	private final JFrame frame;
	private ClientImpl client;

	private ClientView() {
		frame = new JFrame("Client") {
			@Override
			public void dispose() {
				client.shutDown();
				super.dispose();
			}
		};
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setLayout(new GridLayout(2, 1));

		JButton pythonTaskButton = new JButton("Create a task");
		pythonTaskButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PythonEditor.newPythonEditor(ClientView.this);
			}
		});

		JButton exitButton = new JButton("Exit");
		exitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}
		});

		frame.add(pythonTaskButton);
		frame.add(exitButton);

		frame.pack();
	}

	public void show(ClientImpl client) {
		this.client = client;
		frame.setVisible(true);
	}

	public void showConnectionError() {
		JOptionPane.showMessageDialog(frame, "Connection to server was lost.");
	}

	public void showResult(Result<?> result) {
		try {
			JOptionPane.showMessageDialog(frame, result.get());
		} catch (ExecutionException e) {
			JOptionPane.showMessageDialog(frame, "Something went wrong when running the task.");
		}
	}

	public void dispose() {
		frame.dispose();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Task task = PythonTask.newPythonTask(((JTextArea) e.getSource()).getText());
		ClientTaskId id = client.addTask(task);
		client.sendTask(id);
	}

	public static ClientView newView() {
		return new ClientView();
	}
}
