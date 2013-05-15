package se.sdmapeg.client.gui;

import javax.swing.JPanel;

import se.sdmapeg.client.gui.tasks.PrimeFactorTaskView;
import se.sdmapeg.client.gui.tasks.PythonTask.PythonTaskView;

public enum TaskType {
	PYTHON_TASK("Python Task"), PRIME_FACTOR_TASK("Prime Factor Task");

	private final String name;

	private TaskType(String name) {
		this.name = name;
	}

	public JPanel getObject() {
		switch(this){
		case PYTHON_TASK:
			return new PythonTaskView();
		case PRIME_FACTOR_TASK:
			return new PrimeFactorTaskView();
		default:
			return new JPanel();
		}
	}

	@Override
	public String toString(){
		return name;
	}
}
