package se.sdmapeg.worker.gui;

import se.sdmapeg.common.TimeFormatter;
import se.sdmapeg.serverworker.TaskId;
import se.sdmapeg.worker.Worker;
import se.sdmapeg.worker.WorkerListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public final class WorkerView extends JFrame implements TaskView.TaskViewCallback {
	private final Worker worker;
	private long startTime;
	private int totalTasks = 0;
	private int tasksPerformedCounter = 0;
	private Map<TaskId, TaskView> taskViews;

	private JLabel timer;
	private JLabel tasksReceived;
	private JLabel tasksPerformed;
	private JLabel activeTasks;
	private JLabel queueLength;
	private JPanel workList;

	public WorkerView(Worker worker) {
		this.worker = worker;
		this.startTime = System.currentTimeMillis();
		worker.addListener(new WorkerListenerImpl());
		taskViews = new HashMap<>();

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle("SDMAPeG Worker");
		setLayout(new BorderLayout());
		getRootPane().setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

		JPanel content = new JPanel(new GridLayout(1, 2, 10, 0));
		add(content, BorderLayout.CENTER);

		JButton exit = new JButton("Exit");
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		add(exit, BorderLayout.SOUTH);

		JPanel statistics = new JPanel(new BorderLayout());
		content.add(statistics);

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

		JLabel ConnectedToLabel = new JLabel("Connected to: ", SwingConstants.RIGHT);
		JLabel ConnectedTo = new JLabel(worker.getHost());
		labels.add(ConnectedToLabel);
		values.add(ConnectedTo);

		JLabel tasksReceivedLabel = new JLabel("Number of received tasks: ", SwingConstants.RIGHT);
		tasksReceived = new JLabel(Integer.toString(totalTasks));
		labels.add(tasksReceivedLabel);
		values.add(tasksReceived);

		JLabel tasksPerformedLabel = new JLabel("Number of performed tasks: ", SwingConstants.RIGHT);
		tasksPerformed = new JLabel(Integer.toString(tasksPerformedCounter));
		labels.add(tasksPerformedLabel);
		values.add(tasksPerformed);

		int activeTasksCount = Math.min(totalTasks - tasksPerformedCounter, Runtime.getRuntime().availableProcessors());
		JLabel activeTasksLabel = new JLabel("Number of tasks running: ", SwingConstants.RIGHT);
		activeTasks = new JLabel(Integer.toString(activeTasksCount));
		labels.add(activeTasksLabel);
		values.add(activeTasks);

		JLabel queueLengthLabel = new JLabel("Queue length: ", SwingConstants.RIGHT);
		queueLength = new JLabel(Integer.toString(totalTasks - tasksPerformedCounter - activeTasksCount));
		labels.add(queueLengthLabel);
		values.add(queueLength);

		workList = new JPanel(new GridLayout(0, 1));
		JScrollPane workListScrollPane = new JScrollPane(workList);
		content.add(workListScrollPane);

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
		int activeTasksCount = Math.min(totalTasks - tasksPerformedCounter, Runtime.getRuntime().availableProcessors());
		tasksReceived.setText(Integer.toString(totalTasks));
		tasksPerformed.setText(Integer.toString(tasksPerformedCounter));
		activeTasks.setText(Integer.toString(activeTasksCount));
		queueLength.setText(Integer.toString(totalTasks - tasksPerformedCounter - activeTasksCount));
	}

	@Override
	public void dispose() {
		worker.stop();
		super.dispose();
	}

	@Override
	public void taskRemoved(JPanel panel) {
		workList.remove(panel);
		repaint();
	}

	public class WorkerListenerImpl implements WorkerListener {
		@Override
		public void taskAdded(TaskId taskId) {
			totalTasks++;
			TaskView taskView = new TaskView(WorkerView.this, "Task");
			workList.add(taskView);
			taskViews.put(taskId, taskView);
			updateStatistics();
		}

		@Override
		public void taskStarted(TaskId taskId) {
			TaskView taskView = taskViews.get(taskId);
			if (taskView != null) {
				taskView.taskStarted(taskId);
			}
		}

		@Override
		public void taskFinished(TaskId taskId) {
			tasksPerformedCounter++;
			updateStatistics();
			TaskView taskView = taskViews.get(taskId);
			if (taskView != null) {
				taskView.taskFinished(taskId);
			}
		}

		@Override
		public void taskCancelled(TaskId taskId) {
			tasksPerformedCounter++;
			updateStatistics();
			TaskView taskView = taskViews.get(taskId);
			if (taskView != null) {
				taskView.taskCancelled(taskId);
			}
		}

		@Override
		public void taskStolen(TaskId taskId) {
			totalTasks--;
			updateStatistics();
			TaskView taskView = taskViews.get(taskId);
			if (taskView != null) {
				taskView.taskStolen(taskId);
			}
		}
	}
}
