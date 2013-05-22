package se.sdmapeg.client.gui.tasks;

import se.sdmapeg.client.gui.listeners.TaskCreationListener;
import se.sdmapeg.common.tasks.PrimeFactorsTask;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class PrimeFactorTaskView extends JPanel {
	TaskCreationListener listener;

	public PrimeFactorTaskView(TaskCreationListener listener){
		this.listener = listener;
		
		setLayout(new GridLayout(0, 1));

		final JTextField inData = new JTextField();
		inData.setPreferredSize(new Dimension(120, 25));
		
		JButton submitButton = new JButton("Submit Task");	
		submitButton.setPreferredSize(new Dimension(120, 30));
		submitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					long number = Long.parseLong(inData.getText());
					PrimeFactorsTask task = PrimeFactorsTask.newPrimeFactorTask(number);
					PrimeFactorTaskView.this.listener.taskFinished(task);
				} catch(NumberFormatException ex) {
					JOptionPane.showMessageDialog(PrimeFactorTaskView.this, "NaN");
				}

			}
		});

		add(inData);
		add(submitButton);
	}
}
