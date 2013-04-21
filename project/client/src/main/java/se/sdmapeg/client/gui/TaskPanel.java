package se.sdmapeg.client.gui;

import java.util.Date;

import javax.swing.JPanel;

public class TaskPanel extends JPanel {

	public enum TaskState {
		CREATED, SENT, COMPLETED, FAILED;
	}

	Date timeStamp;
	String typeName;
	String name;
	TaskState state;
	
	public TaskPanel(){
		
	}

}
