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
		startTime = System.currentTimeMillis();
		setLayout(new BorderLayout());

		JPanel panel = new JPanel(new BorderLayout());
		add(panel, BorderLayout.NORTH);

		JPanel titles = new JPanel(new GridLayout(0, 1));
		JPanel values = new JPanel(new GridLayout(0, 1));
		panel.add(titles, BorderLayout.WEST);
		panel.add(values, BorderLayout.EAST);

		titles.add(new JPanel());
		titles.add(new JPanel());
		titles.add(new JPanel());
		titles.add(new JPanel());
		values.add(new JPanel());
		values.add(new JPanel());
		values.add(new JPanel());
		values.add(new JPanel());
		titles.add(new JPanel());
		titles.add(new JPanel());
		titles.add(new JPanel());
		titles.add(new JPanel());
		values.add(new JPanel());
		values.add(new JPanel());
		values.add(new JPanel());
		values.add(new JPanel());

		JLabel timerTitle = new JLabel("Time since startup: ");
		timerLabel = new JLabel("00:00:00");
		titles.add(timerTitle);
		values.add(timerLabel);

		JLabel connectedToTitle = new JLabel("Connected to: ");
		connectedToLabel = new JLabel(host);
		titles.add(connectedToTitle);
		values.add(connectedToLabel);

		JLabel tasksTitle = new JLabel("Total tasks: ");
		tasksLabel = new JLabel(Integer.toString(tasks));
		titles.add(tasksTitle);
		values.add(tasksLabel);

		JLabel sentTitle = new JLabel("Tasks sent: ");
		sentLabel = new JLabel(Integer.toString(tasksSent));
		titles.add(sentTitle);
		values.add(sentLabel);

		JLabel receivedTitle = new JLabel("Results received: ");
		receivedLabel = new JLabel(Integer.toString(resultsReceived));
		titles.add(receivedTitle);
		values.add(receivedLabel);

		JLabel cancelledTitle = new JLabel("Tasks cancelled: ");
		cancelledLabel = new JLabel(Integer.toString(tasksCancelled));
		titles.add(cancelledTitle);
		values.add(cancelledLabel);

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
		tasksLabel.setText(Integer.toString(tasks));
		sentLabel.setText(Integer.toString(tasksSent));
		receivedLabel.setText(Integer.toString(resultsReceived));
		cancelledLabel.setText(Integer.toString(tasksCancelled));
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
