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

public class FindNextPrimeTaskView extends JPanel{
	TaskCreationListener listener;
	
	public FindNextPrimeTaskView(TaskCreationListener listener){
		this.listener = listener;
		
		setLayout(new GridLayout(0, 1, 0, 2));

		final JLabel nameLabel = new JLabel("Task name:");
		final JTextField taskName = new JTextField();
		final JLabel inDataLabel = new JLabel("Prime number:");
		final JTextField inData = new JTextField();
		inData.setPreferredSize(new Dimension(120, 25));
		
		JButton submitButton = new JButton("Submit Task");	
		submitButton.setPreferredSize(new Dimension(120, 30));
		submitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					long prime = Long.parseLong(inData.getText());
					FindNextPrimeTask task = FindNextPrimeTask.newFindNextPrimeTask(prime, 
							taskName.getText());
					FindNextPrimeTaskView.this.listener.taskFinished(task);
				} catch(NumberFormatException ex) {
					JOptionPane.showMessageDialog(FindNextPrimeTaskView.this, "NaN");
				}
			}
		});

		add(nameLabel);
		add(taskName);
		add(inDataLabel);
		add(inData);
		add(submitButton);
		
	}
}
