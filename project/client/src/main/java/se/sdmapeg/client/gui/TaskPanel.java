package se.sdmapeg.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Calendar;
import java.util.Date;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import se.sdmapeg.common.TimeFormatter;
import se.sdmapeg.serverclient.ClientTaskId;

public class TaskPanel extends JPanel {

	public enum TaskState {
		CREATED, SENT, COMPLETED, FAILED;
	}

	private final Callback callback;
	private final Calendar timeStamp;
	private final  String typeName;
	private String name;
	private TaskState state;
	private JLabel elapsedTimeLabel;
	private TimeFormatter timeFormatter;
	private JButton actionButton;
	
	
	public TaskPanel(String typeName, Callback callback) {
		this.callback = callback;
		this.typeName = typeName;
		this.timeStamp = Calendar.getInstance();
		this.state = TaskState.CREATED;
		this.name = null;
		this.setLayout(new BorderLayout());
		
		JPanel centerPanel = new JPanel(new GridLayout(1, 2));
		JPanel centerPanelText = new JPanel(new GridLayout(3, 1));
		centerPanel.add(centerPanelText);
		JPanel checkBoxPanel = new JPanel(new BorderLayout());
		JPanel actionButtonPanel = new JPanel(new BorderLayout());
		
		centerPanelText.add(new JLabel(typeName));
		centerPanelText.add(new JLabel("Created: " + timeStamp.get(Calendar.HOUR_OF_DAY) + 
				":" + timeStamp.get(Calendar.MINUTE)));
		elapsedTimeLabel = new JLabel("Elapsed time: 0");
		centerPanelText.add(elapsedTimeLabel);
		centerPanel.add(actionButtonPanel, BorderLayout.CENTER);
		
		actionButton = new JButton("Send task");
		actionButtonPanel.add(actionButton);
		
		JCheckBox selectBox = new JCheckBox();
		checkBoxPanel.add(selectBox, BorderLayout.CENTER );
		checkBoxPanel.add(Box.createRigidArea(new Dimension(10, 0)), BorderLayout.EAST);
		this.add(checkBoxPanel, BorderLayout.WEST);
		this.add(centerPanel, BorderLayout.CENTER);
		this.add(new JButton(), BorderLayout.EAST);
		
		this.setBorder(new LineBorder(Color.BLACK));
		this.setSize(new Dimension(0, 200));
	}
	
	public TaskPanel(String typeName, String name, Callback callback) {
		this(typeName, callback);
		this.name = name;
	}
	
	//TODO: Remove this when done. Duh. 
	/*public static void main(String[] args){
		JFrame frame = new JFrame();
		frame.add(new TaskPanel("PythonTask"));
		frame.setVisible(true);
	}*/
	
	public interface Callback {
		void sendTask(ClientTaskId clientTaskId);
		void cancelTask(ClientTaskId clientTaskId);
	}
}
