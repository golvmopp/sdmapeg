package se.sdmapeg.client.gui;

import se.sdmapeg.client.ClientImpl;
import se.sdmapeg.common.tasks.PythonTask;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.serverclient.ClientTaskId;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutionException;

/**
 * Class that handles the Client gui.
 */
public class ClientView implements ActionListener {
	private final JFrame frame;
	private ClientImpl client;

	private ClientView() {
		frame = new JFrame("Client");
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
				System.exit(0);
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

	public static ClientView newView() {
		return new ClientView();
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
		Task task = PythonTask.newPythonTask(((TextArea) e.getSource()).getText());
		ClientTaskId id = client.addTask(task);
		client.sendTask(id);
	}
}
