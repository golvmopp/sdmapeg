package se.sdmapeg.client.gui.taskcreation;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import se.sdmapeg.client.gui.listeners.TaskCreationListener;
import se.sdmapeg.client.gui.tasks.FindNextPrimeTaskView;
import se.sdmapeg.client.gui.tasks.PrimeFactorTaskView;
import se.sdmapeg.client.gui.tasks.PythonTask.PythonTaskView;

public class TaskCreationView extends JFrame {
	public enum TaskType {
		PYTHON_TASK("Python Task"), PRIME_FACTOR_TASK("Prime Factor Task"), 
		NEXT_PRIME_TASK("Find Next Prime Task");

		private final String name;

		private TaskType(String name) {
			this.name = name;
		}

		@Override
		public String toString(){
			return name;
		}
	}

	TaskCreationListener listener;

	JPanel visiblePanel;

	public TaskCreationView(TaskCreationListener listener){
		this.listener = listener;
		
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

		setVisible(true);
	}

	private JPanel getTaskView(TaskType selectedItem) {
		switch (selectedItem){
			case PYTHON_TASK:
				return new PythonTaskView(listener);
			case PRIME_FACTOR_TASK:
				return new PrimeFactorTaskView(listener);
			case NEXT_PRIME_TASK:
				return new FindNextPrimeTaskView(listener);
			default:
				return new JPanel();
		}
	}
}
