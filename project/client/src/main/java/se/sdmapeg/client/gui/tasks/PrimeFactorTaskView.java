package se.sdmapeg.client.gui.tasks;

import se.sdmapeg.client.gui.TaskCreationCallback;

import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class PrimeFactorTaskView extends JPanel{
	TaskCreationCallback callback;

	public PrimeFactorTaskView(TaskCreationCallback callback){
		this.callback = callback;

		JTextField inData = new JTextField();
		JButton submitButton = new JButton("Submit Task");	
		add(inData);
		add(submitButton);
	}
}
