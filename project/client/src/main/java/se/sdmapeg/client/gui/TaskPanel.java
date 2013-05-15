package se.sdmapeg.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;

import javax.swing.*;
import javax.swing.border.LineBorder;

import se.sdmapeg.client.ClientListener;
import se.sdmapeg.common.TimeFormatter;
import se.sdmapeg.serverclient.ClientTaskId;

public class TaskPanel extends JPanel implements ClientListener {
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
	private JPanel actionButtonPanel;

	private static final Color CREATED = Color.WHITE;
	private static final Color SENT = new Color(195, 200, 72);
	private static final Color COMPLETED = new Color(118, 217, 101);
	private static final Color FAILED = new Color(255, 91, 90);

	private final ClientTaskId clientTaskId;
	
	
	public TaskPanel(String typeName, final Callback callback, final ClientTaskId clientTaskId) {
		this.callback = callback;
		this.typeName = typeName;
		this.timeStamp = Calendar.getInstance();
		this.state = TaskState.CREATED;
		this.name = null;
		this.setLayout(new BorderLayout());
		this.clientTaskId = clientTaskId;
		
		JPanel centerPanel = new JPanel(new GridLayout(1, 2));
		centerPanel.setOpaque(false);
		JPanel centerPanelText = new JPanel(new GridLayout(3, 1));
		centerPanelText.setOpaque(false);
		centerPanel.add(centerPanelText);
		JPanel checkBoxPanel = new JPanel(new BorderLayout());
		checkBoxPanel.setOpaque(false);
		actionButtonPanel = new JPanel(new BorderLayout());
		actionButtonPanel.setOpaque(false);
		
		centerPanelText.add(new JLabel(typeName));
		centerPanelText.add(new JLabel("Created: " + timeStamp.get(Calendar.HOUR_OF_DAY) + 
				":" + timeStamp.get(Calendar.MINUTE)));
		elapsedTimeLabel = new JLabel("Elapsed time: 0");
		centerPanelText.add(elapsedTimeLabel);
		centerPanel.add(actionButtonPanel, BorderLayout.CENTER);
		
		actionButton = new JButton("Send task");
		actionButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (state == TaskState.CREATED) {
					send();
				} else if (state == TaskState.SENT) {
					cancel();
				} else {
					callback.showResult(clientTaskId);
				}
			}
		});
		actionButtonPanel.add(actionButton, BorderLayout.CENTER);
		
		JCheckBox selectBox = new JCheckBox();
		checkBoxPanel.add(selectBox, BorderLayout.CENTER);
		checkBoxPanel.add(Box.createRigidArea(new Dimension(10, 0)), BorderLayout.EAST);
		this.add(checkBoxPanel, BorderLayout.WEST);
		this.add(centerPanel, BorderLayout.CENTER);

		JButton cancelButton = new JButton("X");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				callback.removeTaskPanel(clientTaskId, TaskPanel.this);
			}
		});
		cancelButton.setVerticalAlignment(SwingConstants.TOP);
		cancelButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.add(cancelButton, BorderLayout.EAST);
		
		this.setBorder(new LineBorder(Color.BLACK));
		this.setSize(new Dimension(0, 200));
	}
	
	public TaskPanel(String typeName, String name, Callback callback, ClientTaskId clientTaskId) {
		this(typeName, callback, clientTaskId);
		this.name = name;
	}

	private void cancel() {
		callback.cancelTask(clientTaskId);
		taskCancelled(clientTaskId);
	}

	private void send() {
		state = TaskState.SENT;
		callback.sendTask(clientTaskId);
		setBackground(SENT);
		actionButton.setText("Cancel");
	}

	@Override
	public void taskCreated(ClientTaskId clientTaskId) {}
	@Override
	public void taskSent(ClientTaskId clientTaskId) {}

	@Override
	public void taskCancelled(ClientTaskId clientTaskId) {
		state = TaskState.FAILED;
		setBackground(FAILED);
		actionButtonPanel.removeAll();
		repaint();
	}

	@Override
	public void resultReceived(final ClientTaskId clientTaskId) {
		state = TaskState.COMPLETED;
		setBackground(COMPLETED);
		actionButton.setText("Show result");
	}
	
	public interface Callback {
		void sendTask(ClientTaskId clientTaskId);
		void cancelTask(ClientTaskId clientTaskId);
		void showResult(ClientTaskId clientTaskId);
		void removeTaskPanel(ClientTaskId clientTaskId, JPanel panel);
	}
}
