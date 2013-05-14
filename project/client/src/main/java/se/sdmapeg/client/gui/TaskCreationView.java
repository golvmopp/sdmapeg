package se.sdmapeg.client.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import se.sdmapeg.client.gui.tasks.PythonTask.PythonTaskView;
import se.sdmapeg.client.gui.tasks.PythonTask.PythonTaskView;

public class TaskCreationView extends JFrame {
	
	
	JPanel mainPanel;
	
	public TaskCreationView(){
		setLayout(new BorderLayout());
		final CardLayout cl = new CardLayout();
		mainPanel = new JPanel();
		
		mainPanel.setLayout(cl);

		PythonTaskView pythonTaskPanel = new PythonTaskView();
		
		//TODO: Move this to separate class
		JPanel primeFactorTaskPanel = new JPanel(new GridLayout(1, 0));
		JTextField inData = new JTextField();
		JButton submitButton = new JButton("Submit Task");	
		primeFactorTaskPanel.add(inData);
		primeFactorTaskPanel.add(submitButton);
		cl.addLayoutComponent(pythonTaskPanel, "PythonTask");
		cl.addLayoutComponent(primeFactorTaskPanel, "PrimeFactorTask");
		
		final JComboBox<String> taskSelector = new JComboBox<String>();
		this.add(taskSelector, BorderLayout.NORTH);
		taskSelector.addItemListener(new ItemListener() {			
			@Override
			public void itemStateChanged(ItemEvent e) {
				cl.show(mainPanel, (String) taskSelector.getSelectedItem());
			}
		});
		
		setVisible(true);
		pack();
	}
}
