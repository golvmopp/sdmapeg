package se.sdmapeg.client.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
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
import se.sdmapeg.common.tasks.Task;

public class TaskCreationView extends JFrame implements TaskCreationCallback {
	TaskCreationCallback callback;

	JPanel mainPanel;
	JPanel visiblePanel;

	public TaskCreationView(TaskCreationCallback callback){
		this.callback = callback;
		
		setLayout(new BorderLayout(15, 0));
		
		final JComboBox<TaskType> taskSelector = new JComboBox<>();
		for(TaskType type : TaskType.values()){
			taskSelector.addItem(type);
		}
		
		this.add(taskSelector, BorderLayout.NORTH);
		visiblePanel = getTaskView(TaskType.values()[0]);
		this.add(visiblePanel, BorderLayout.CENTER);
		
		taskSelector.addItemListener(new ItemListener() {			
			@Override
			public void itemStateChanged(ItemEvent e) {
				remove(visiblePanel);
				visiblePanel = getTaskView((TaskType) taskSelector.getSelectedItem());
				add(visiblePanel, BorderLayout.CENTER);
				TaskCreationView.this.pack();
			}
		});
		
		pack();
		setVisible(true);
	}

	private JPanel getTaskView(TaskType selectedItem) {
		switch (selectedItem){
			case PYTHON_TASK:
				return new PythonTaskView(this);
			case PRIME_FACTOR_TASK:
				return new PrimeFactorTaskView(this);
			default:
				return new JPanel();
		}
	}

	@Override
	public void addTask(Task task) {
		callback.addTask(task);
		this.dispose();
	}
}
