package se.sdmapeg.client.gui.views;

import se.sdmapeg.client.models.ClientListener;
import se.sdmapeg.common.TimeFormatter;
import se.sdmapeg.serverclient.ClientTaskId;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StatisticsView extends JPanel implements ClientListener {
	private int tasks = 0;
	private int tasksSent = 0;
	private int tasksCancelled = 0;
	private int resultsReceived = 0;
	private long startTime;

	private JLabel tasksLabel;
	private JLabel sentLabel;
	private JLabel cancelledLabel;
	private JLabel receivedLabel;
	private JLabel connectedToLabel;
	private JLabel timerLabel;

	public StatisticsView(String host) {
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		startTime = System.currentTimeMillis();
		setLayout(new GridLayout(2, 4, 0, 5));

		JLabel timerTitle = new JLabel("Uptime: ");
		timerLabel = new JLabel("00:00:00");
		add(timerTitle);
		add(timerLabel);

		JLabel connectedToTitle = new JLabel("Connected to: ");
		connectedToLabel = new JLabel(host);
		add(connectedToTitle);
		add(connectedToLabel);

		tasksLabel = new JLabel("Tasks: 0");
		add(tasksLabel);

		sentLabel = new JLabel("Sent: 0");
		add(sentLabel);

		receivedLabel = new JLabel("Done: 0");
		add(receivedLabel);

		cancelledLabel = new JLabel("Cancelled: 0");
		add(cancelledLabel);

		new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateTimer();
			}
		}).start();
	}

	private void updateTimer() {
		TimeFormatter timeFormatter = new TimeFormatter(startTime);
		timerLabel.setText(timeFormatter.getFormattedHours() + ":" + timeFormatter.getFormattedMinutes() + ":" + timeFormatter.getFormattedSeconds());
	}

	private void updateStatistics() {
		tasksLabel.setText("Tasks: " + Integer.toString(tasks));
		sentLabel.setText("Sent: " + Integer.toString(tasksSent));
		receivedLabel.setText("Done: " + Integer.toString(resultsReceived));
		cancelledLabel.setText("Cancelled: " + Integer.toString(tasksCancelled));
	}

	@Override
	public void taskCreated(ClientTaskId clientTaskId) {
		tasks++;
		updateStatistics();
	}

	@Override
	public void taskSent(ClientTaskId clientTaskId) {
		tasksSent++;
		updateStatistics();
	}

	@Override
	public void taskCancelled(ClientTaskId clientTaskId) {
		tasksCancelled++;
		updateStatistics();
	}

	@Override
	public void resultReceived(ClientTaskId clientTaskId) {
		resultsReceived++;
		updateStatistics();
	}
}
