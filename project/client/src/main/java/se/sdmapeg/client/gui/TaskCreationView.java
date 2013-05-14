package se.sdmapeg.client.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class TaskCreationView extends JFrame {
	
	
	JPanel mainPanel;
	
	public TaskCreationView(){
		this.setLayout(new BorderLayout());
		CardLayout cl = new CardLayout();
		
		mainPanel.setLayout(cl);
		
		JPanel pythonPanel = new JPanel(new GridLayout(0, 1));
		
		cl.addLayoutComponent(pythonPanel, "PythonTaskView");
		
		JComboBox<String> taskSelector = new JComboBox<String>();
		this.add(taskSelector, BorderLayout.NORTH);
		taskSelector.addItemListener(new ItemListener() {			
			@Override
			public void itemStateChanged(ItemEvent e) {
				//TODO: Set cardlayout to change
			}
		});
	}
}
