package se.sdmapeg.worker.gui;

import se.sdmapeg.common.TimeFormatter;
import se.sdmapeg.serverworker.TaskId;
import se.sdmapeg.worker.Worker;
import se.sdmapeg.worker.WorkerListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public final class WorkerView extends JFrame {
	private final Worker worker;
	private long startTime;
	private int totalTasks = 0;
	private int tasksPerformedCounter = 0;

	private JLabel timer;
	private JLabel tasksReceived;
	private JLabel tasksPerformed;
	private JLabel activeTasks;
	private JLabel queueLength;

	public WorkerView(Worker worker) {
		this.worker = worker;
		this.startTime = System.currentTimeMillis();
		worker.addListener(new WorkerListenerImpl());

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle("SDMAPeG Worker");
		setLayout(new GridLayout(1, 2));
		getRootPane().setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

		JPanel statistics = new JPanel(new BorderLayout());
		add(statistics);

		JPanel labels = new JPanel(new GridLayout(0, 1));
		JPanel values = new JPanel(new GridLayout(0, 1));
		statistics.add(labels, BorderLayout.WEST);
		statistics.add(values, BorderLayout.EAST);

		JLabel timerLabel = new JLabel("Time since startup: ", SwingConstants.RIGHT);
		timer = new JLabel("00:00:00");
		labels.add(timerLabel);
		values.add(timer);

		JLabel availableProcessorsLabel = new JLabel("Available Processors: ", SwingConstants.RIGHT);
		JLabel availableProcessors = new JLabel(Integer.toString(Runtime.getRuntime().availableProcessors()));
		labels.add(availableProcessorsLabel);
		values.add(availableProcessors);

		JLabel tasksReceivedLabel = new JLabel("Number of received tasks: ", SwingConstants.RIGHT);
		tasksReceived = new JLabel(Integer.toString(totalTasks));
		labels.add(tasksReceivedLabel);
		values.add(tasksReceived);

		JLabel tasksPerformedLabel = new JLabel("Number of performed tasks: ", SwingConstants.RIGHT);
		tasksPerformed = new JLabel(Integer.toString(tasksPerformedCounter));
		labels.add(tasksPerformedLabel);
		values.add(tasksPerformed);

		JLabel activeTasksLabel = new JLabel("Number of tasks running: ", SwingConstants.RIGHT);
		activeTasks = new JLabel(Integer.toString(Math.min(totalTasks - tasksPerformedCounter, Runtime.getRuntime().availableProcessors())));
		labels.add(activeTasksLabel);
		values.add(activeTasks);

		JLabel queueLengthLabel = new JLabel("Queue length: ", SwingConstants.RIGHT);
		queueLength = new JLabel(Integer.toString(totalTasks - tasksPerformedCounter));
		labels.add(queueLengthLabel);
		values.add(queueLength);

		add(statistics);

		JPanel workList = new JPanel(new GridLayout(0, 1));
		JScrollPane workListScrollPane = new JScrollPane(workList);

		pack();
		setVisible(true);

		new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateTimer();
			}
		}).start();
	}

	private void updateTimer() {
		TimeFormatter timeFormatter = new TimeFormatter(startTime);
		timer.setText(timeFormatter.getFormattedHours() + ":" + timeFormatter.getFormattedMinutes() + ":" + timeFormatter.getFormattedSeconds());
	}

	private void updateStatistics() {
		tasksReceived.setText(Integer.toString(totalTasks));
		tasksPerformed.setText(Integer.toString(tasksPerformedCounter));
		activeTasks.setText(Integer.toString(Math.min(totalTasks - tasksPerformedCounter, Runtime.getRuntime().availableProcessors())));
		queueLength.setText(Integer.toString(totalTasks - tasksPerformedCounter));
		System.out.println("Statistics updated.");
	}

	@Override
	public void dispose() {
		worker.stop();
	}

	public class WorkerListenerImpl implements WorkerListener {

		@Override
		public void taskAdded(TaskId taskId) {
			totalTasks++;
			updateStatistics();
		}

		@Override
		public void taskStarted(TaskId taskId) {
			//To change body of implemented methods use File | Settings | File Templates.
		}

		@Override
		public void taskFinished(TaskId taskId) {
			tasksPerformedCounter++;
			updateStatistics();
		}

		@Override
		public void taskCancelled(TaskId taskId) {
			tasksPerformedCounter++;
			updateStatistics();
		}

		@Override
		public void taskStolen(TaskId taskId) {
			totalTasks--;
			updateStatistics();
		}
	}
}
