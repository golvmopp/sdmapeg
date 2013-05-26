package se.sdmapeg.client.gui.views.tasks;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import se.sdmapeg.client.gui.listeners.TaskCreationListener;
import se.sdmapeg.common.tasks.FindNextPrimeTask;
import se.sdmapeg.common.tasks.IdleTask;

public class IdleTaskView extends JPanel {
	
	TaskCreationListener listener;

	public IdleTaskView(TaskCreationListener listener){
		this.listener = listener;
		
		setLayout(new GridLayout(0, 1, 0, 2));

		final JLabel nameLabel = new JLabel("Task name:");
		final JTextField taskName = new JTextField();

		
		JButton submitButton = new JButton("Submit Task");	
		submitButton.setPreferredSize(new Dimension(120, 30));
		submitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				IdleTask task = IdleTask.newIdleTask(taskName.getText());
				IdleTaskView.this.listener.taskFinished(task);
				}
		});

		add(nameLabel);
		add(taskName);
		add(submitButton);
		
}
	
	
}
