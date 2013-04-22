package se.sdmapeg.client.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class TaskPanel extends JPanel {

	public enum TaskState {
		CREATED, SENT, COMPLETED, FAILED;
	}

	private final Calendar timeStamp;
	private final  String typeName;
	private String name;
	private TaskState state;
	private JLabel elapsedTime;
	
	public TaskPanel(String typeName) {
		this.typeName = typeName;
		this.timeStamp = Calendar.getInstance();
		this.state = TaskState.CREATED;
		this.name = null;
		this.setLayout(new BorderLayout());
		
		JPanel centerPanel = new JPanel(new GridLayout(1, 2));
		JPanel centerPanelText = new JPanel(new GridLayout(3, 1));
		JPanel centerPanelCancel = new JPanel();
		centerPanel.add(centerPanelText);
		centerPanel.add(centerPanelCancel);
		JPanel checkBoxPanel = new JPanel();
		JPanel closeButtonPanel = new JPanel();
		
		centerPanelText.add(new JLabel("" + timeStamp.get(Calendar.HOUR_OF_DAY) + 
				timeStamp.get(Calendar.MINUTE)));
		centerPanelText.add(new JLabel(typeName));
		centerPanelText.add(new JLabel(state.name()));
		centerPanelText.add(new JLabel(""));
		
		this.add(checkBoxPanel, BorderLayout.WEST);
		this.add(closeButtonPanel, BorderLayout.EAST);
		this.add(centerPanel, BorderLayout.CENTER);
		
		
	}
	
	public TaskPanel(String typeName, String name) {
		this(typeName);
		this.name = name;
	}
	
	public void updateTimeStamp() {
		
	}
	
	
}
