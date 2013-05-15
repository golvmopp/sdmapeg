package se.sdmapeg.client.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import se.sdmapeg.client.gui.tasks.PrimeFactorTaskView;
import se.sdmapeg.client.gui.tasks.PythonTask.PythonTaskView;
import se.sdmapeg.client.gui.tasks.PythonTask.PythonTaskView;

public class TaskCreationView extends JFrame {
	
	
	JPanel mainPanel;
	
	public TaskCreationView(){
		setLayout(new BorderLayout(15, 0));
		
		final JComboBox<TaskType> taskSelector = new JComboBox<>();
		for(TaskType type : TaskType.values()){
			taskSelector.addItem(type);
		}
		
		this.add(taskSelector, BorderLayout.NORTH);
		this.add(TaskType.values()[0].getObject(), BorderLayout.CENTER);
		
		taskSelector.addItemListener(new ItemListener() {			
			@Override
			public void itemStateChanged(ItemEvent e) {
				TaskCreationView.this.add(addTaskView((TaskType)taskSelector.
						getSelectedItem()), BorderLayout.CENTER);
			}

			private Component addTaskView(TaskType selectedItem) {
				switch (selectedItem){
				case PYTHON_TASK:
					return new PythonTaskView();
				case PRIME_FACTOR_TASK:
					return new PrimeFactorTaskView();
				default:
					return new JPanel();
				}
			}
		});
		
		setVisible(true);
		pack();
	}
}
