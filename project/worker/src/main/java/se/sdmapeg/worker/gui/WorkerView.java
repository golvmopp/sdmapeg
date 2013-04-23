package se.sdmapeg.worker.gui;

import se.sdmapeg.serverworker.TaskId;
import se.sdmapeg.worker.Worker;
import se.sdmapeg.worker.WorkerListener;

import javax.swing.*;
import java.awt.*;

public final class WorkerView extends JFrame implements WorkerListener {
	private final Worker worker;
	private long startTime;
	private int totalTasks = 0;
	private int tasksPerformedCounter = 0;

	public WorkerView(Worker worker) {
		this.worker = worker;
		this.startTime = System.currentTimeMillis();

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle("SDMAPeG Worker");
		setLayout(new GridLayout(1, 2));

		JPanel statistics = new JPanel(new GridLayout(0, 2));

		JLabel timerLabel = new JLabel("Time since startup: ", SwingConstants.RIGHT);
		JLabel timer = new JLabel("00:00:00");
		statistics.add(timerLabel);
		statistics.add(timer);

		JLabel availableProcessorsLabel = new JLabel("Available Processors: ", SwingConstants.RIGHT);
		JLabel availableProcessors = new JLabel(Integer.toString(Runtime.getRuntime().availableProcessors()));
		statistics.add(availableProcessorsLabel);
		statistics.add(availableProcessors);

		JLabel tasksReceivedLabel = new JLabel("Number of received tasks: ", SwingConstants.RIGHT);
		JLabel tasksReceived = new JLabel(Integer.toString(totalTasks));
		statistics.add(tasksReceivedLabel);
		statistics.add(tasksReceived);

		JLabel tasksPerformedLabel = new JLabel("Number of performed tasks: ", SwingConstants.RIGHT);
		JLabel tasksPerformed = new JLabel(Integer.toString(tasksPerformedCounter));
		statistics.add(tasksPerformedLabel);
		statistics.add(tasksPerformed);

		JLabel activeTasksLabel = new JLabel("Number of tasks running: ", SwingConstants.RIGHT);
		JLabel activeTasks = new JLabel(Integer.toString(Math.min(totalTasks - tasksPerformedCounter, Runtime.getRuntime().availableProcessors())));
		statistics.add(activeTasksLabel);
		statistics.add(activeTasks);

		JLabel queueLengthLabel = new JLabel("Queue length: ", SwingConstants.RIGHT);
		JLabel queueLength = new JLabel(Integer.toString(totalTasks - tasksPerformedCounter));
		statistics.add(queueLengthLabel);
		statistics.add(queueLength);

		add(statistics);

		JPanel workList = new JPanel(new GridLayout(0, 1));
		JScrollPane workListScrollPane = new JScrollPane(workList);

		pack();
		setVisible(true);
	}

	@Override
	public void dispose() {
		worker.stop();
	}

	@Override
	public void taskAdded(TaskId taskId) {
		totalTasks++;
	}

	@Override
	public void taskStarted(TaskId taskId) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void taskFinished(TaskId taskId) {
		tasksPerformedCounter++;
	}

	@Override
	public void taskCancelled(TaskId taskId) {
		// TODO: implement this
	}

	@Override
	public void taskStolen(TaskId taskId) {
		// TODO: implement this
	}

	public static void main(String[] args) {
		new WorkerView(null);
	}
}
