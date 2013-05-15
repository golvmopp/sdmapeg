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
	private final ClientTaskId clientTaskId;
	private TaskState state;
	private long startTime;
	private Calendar timeStamp;

	private String name;
	private final String typeName;

	private Timer timer;

	private JLabel elapsedTimeLabel;
	private JButton actionButton;
	private JPanel actionButtonPanel;
	private JCheckBox selectBox;

	private static final Color CREATED = Color.WHITE;
	private static final Color SENT = new Color(195, 200, 72);
	private static final Color COMPLETED = new Color(118, 217, 101);
	private static final Color FAILED = new Color(255, 91, 90);
	
	
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
		centerPanelText.add(new JLabel("Created: " +
				                               TimeFormatter.addLeadingZeros(
						                               Integer.toString(timeStamp.get(Calendar.HOUR_OF_DAY)), 2) +
				                               ":" + TimeFormatter.addLeadingZeros(
				Integer.toString(timeStamp.get(Calendar.MINUTE)), 2)));
		elapsedTimeLabel = new JLabel("Time: 00:00:00");
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
		actionButton.setPreferredSize(new Dimension(100, 0));
		actionButtonPanel.add(actionButton, BorderLayout.CENTER);
		
		selectBox = new JCheckBox();
		checkBoxPanel.add(selectBox, BorderLayout.CENTER);
		checkBoxPanel.add(Box.createRigidArea(new Dimension(10, 0)), BorderLayout.EAST);
		this.add(checkBoxPanel, BorderLayout.WEST);
		this.add(centerPanel, BorderLayout.CENTER);

		JButton cancelButton = new JButton("X");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				callback.taskRemoved(clientTaskId, TaskPanel.this);
			}
		});
		cancelButton.setVerticalAlignment(SwingConstants.TOP);
		cancelButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.add(cancelButton, BorderLayout.EAST);
		
		this.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
	}
	
	public TaskPanel(String typeName, String name, Callback callback, ClientTaskId clientTaskId) {
		this(typeName, callback, clientTaskId);
		this.name = name;
	}

	private void updateTimer() {
		TimeFormatter timeFormatter = new TimeFormatter(startTime);
		elapsedTimeLabel.setText("Time: " + timeFormatter.getFormattedHours() + ":" + timeFormatter.getFormattedMinutes() + ":" + timeFormatter.getFormattedSeconds());
	}

	public boolean isChecked() {
		return selectBox.isSelected();
	}

	private void cancel() {
		callback.cancelTask(clientTaskId);
		taskCancelled(clientTaskId);
	}

	private void send() {
		callback.sendTask(clientTaskId);
		taskSent(clientTaskId);
	}

	@Override
	public void taskCreated(ClientTaskId clientTaskId) {}
	@Override
	public void taskSent(ClientTaskId clientTaskId) {
		state = TaskState.SENT;
		setBackground(SENT);
		actionButton.setText("Cancel");
		selectBox.setSelected(false);

		this.startTime = System.currentTimeMillis();
		timer = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateTimer();
			}
		});
		timer.start();
	}

	@Override
	public void taskCancelled(ClientTaskId clientTaskId) {
		state = TaskState.FAILED;
		setBackground(FAILED);
		actionButtonPanel.removeAll();
		selectBox.setSelected(false);
		timer.stop();
		repaint();
	}

	@Override
	public void resultReceived(final ClientTaskId clientTaskId) {
		state = TaskState.COMPLETED;
		setBackground(COMPLETED);
		actionButton.setText("Show result");
		timer.stop();
	}
	
	public interface Callback {
		void sendTask(ClientTaskId clientTaskId);
		void cancelTask(ClientTaskId clientTaskId);
		void showResult(ClientTaskId clientTaskId);
		void taskRemoved(ClientTaskId clientTaskId, JPanel panel);
	}
}
