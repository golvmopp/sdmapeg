package se.sdmapeg.client.gui.tasks;

import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class PrimeFactorTaskView extends JPanel{
	
	public PrimeFactorTaskView(){
		JTextField inData = new JTextField();
		JButton submitButton = new JButton("Submit Task");	
		add(inData);
		add(submitButton);
	}
}
