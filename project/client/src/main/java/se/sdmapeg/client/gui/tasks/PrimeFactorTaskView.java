package se.sdmapeg.client.gui.tasks;

import se.sdmapeg.client.gui.TaskCreationCallback;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class PrimeFactorTaskView extends JPanel{
	TaskCreationCallback callback;

	public PrimeFactorTaskView(TaskCreationCallback callback){
		this.callback = callback;
		
		setLayout(new GridLayout(0, 1));

		JTextField inData = new JTextField();
		inData.setPreferredSize(new Dimension(120, 25));
		
		JButton submitButton = new JButton("Submit Task");	
		submitButton.setPreferredSize(new Dimension(120, 30));
		add(inData);
		add(submitButton);
	}
}
