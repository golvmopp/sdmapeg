package se.sdmapeg.client.gui.taskmanagement;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;

import se.sdmapeg.client.gui.listeners.TaskPanelListener;
import se.sdmapeg.client.models.ClientListener;
import se.sdmapeg.common.TimeFormatter;
import se.sdmapeg.serverclient.ClientTaskId;

public class TaskPanel extends JPanel implements ClientListener {
	private final TaskModel model;
	private final TaskPanelListener listener;
	private Timer timer;

	private JLabel elapsedTimeLabel;
	private JButton actionButton;
	private JCheckBox checkBox;

	private static final Color CREATED = Color.WHITE;
	private static final Color SENT = new Color(195, 200, 72);
	private static final Color COMPLETED = new Color(118, 217, 101);
	private static final Color FAILED = new Color(255, 91, 90);
	
	
	public TaskPanel(TaskModel model, TaskPanelListener listener, int width) {
		this.model = model;
		this.listener = listener;
		this.setLayout(new BorderLayout());

		setPreferredSize(new Dimension(width, 40));
		setBorder(new BevelBorder(2, Color.BLACK, Color.BLACK));
		
		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.setOpaque(false);
		JPanel centerPanelText = new JPanel(new GridLayout(3, 1));
		centerPanelText.setOpaque(false);
		centerPanel.add(centerPanelText, BorderLayout.CENTER);
		JPanel checkBoxPanel = new JPanel(new BorderLayout());
		checkBoxPanel.setOpaque(false);
		
		
		centerPanelText.add(new JLabel(model.getTypeName() +": "+ model.getName()));
		centerPanelText.add(new JLabel("Created: " +
				                               TimeFormatter.addLeadingZeros(
						                               Integer.toString(model.getTimeStamp().get(Calendar

								                                                                         .HOUR_OF_DAY)),
						                               2) +
				                               ":" + TimeFormatter.addLeadingZeros(
				Integer.toString(model.getTimeStamp().get(Calendar.MINUTE)), 2)));
		elapsedTimeLabel = new JLabel("Time: 00:00:00");
		centerPanelText.add(elapsedTimeLabel);
		
		actionButton = new JButton("Send task");
		actionButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		actionButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (TaskPanel.this.model.getState() == TaskModel.TaskState.CREATED) {
					TaskPanel.this.listener.sendButtonPressed();
				} else if (TaskPanel.this.model.getState() == TaskModel.TaskState.SENT) {
					TaskPanel.this.listener.cancelButtonPressed();
				} else {
					TaskPanel.this.listener.showResultButtonPressed();
				}
			}
		});
		centerPanel.add(actionButton, BorderLayout.EAST);
		
		checkBox = new JCheckBox();
		checkBox.setOpaque(false);
		checkBoxPanel.add(checkBox, BorderLayout.CENTER);
		checkBoxPanel.add(Box.createRigidArea(new Dimension(10, 0)), BorderLayout.EAST);
		checkBoxPanel.setOpaque(false);
		this.add(checkBoxPanel, BorderLayout.WEST);
		this.add(centerPanel, BorderLayout.CENTER);

		JButton cancelButton = new JButton("X");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TaskPanel.this.listener.removeButtonPressed();
			}
		});
		cancelButton.setVerticalAlignment(SwingConstants.TOP);
		cancelButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.add(cancelButton, BorderLayout.EAST);
		
		this.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));

		timer = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateTimer();
			}
		});
	}

	private void updateTimer() {
		TimeFormatter timeFormatter = new TimeFormatter(model.getStartTime());
		elapsedTimeLabel.setText("Time: " + timeFormatter.getFormattedHours() + ":" + timeFormatter.getFormattedMinutes() + ":" + timeFormatter.getFormattedSeconds());
	}

	public boolean isChecked() {
		return checkBox.isSelected();
	}

	@Override
	public void taskCreated(ClientTaskId clientTaskId) {}

	@Override
	public void taskSent(ClientTaskId clientTaskId) {
		setBackground(SENT);
		actionButton.setText("Cancel");
		checkBox.setSelected(false);

		timer.start();
	}

	@Override
	public void taskCancelled(ClientTaskId clientTaskId) {
		setBackground(FAILED);
		checkBox.setSelected(false);
		timer.stop();
		repaint();
	}

	@Override
	public void resultReceived(ClientTaskId clientTaskId) {
		setBackground(COMPLETED);
		actionButton.setText("Show result");
		timer.stop();
	}

}
