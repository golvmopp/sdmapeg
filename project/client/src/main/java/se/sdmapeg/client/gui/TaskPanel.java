package se.sdmapeg.client.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JPanel;

public class TaskPanel extends JPanel {

	public enum TaskState {
		CREATED, SENT, COMPLETED, FAILED;
	}

	private Calendar timeStamp;
	private String typeName;
	private String name;
	private TaskState state;
	
	public TaskPanel(String typeName){
		this.typeName = typeName;
		this.timeStamp = Calendar.getInstance();
		this.state = TaskState.CREATED;
		this.setLayout(new BorderLayout());
		
		JPanel centerPanel = new JPanel(new GridLayout(1, 2));
		JPanel centerPaneltext = new JPanel(new GridLayout(3, 1));
		JPanel centerPanelCancel = new JPanel();
		this.add(centerPanel, BorderLayout.CENTER);
		
	}
	
	public TaskPanel(String typeName, String name){
		this(typeName);
		this.name = name;
	}
}
