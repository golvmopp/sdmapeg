package se.sdmapeg.worker.gui;

import se.sdmapeg.serverworker.TaskId;
import se.sdmapeg.worker.WorkerListener;

import javax.swing.*;
import javax.swing.border.LineBorder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TaskView extends JPanel implements WorkerListener {
	public enum Status {
		RECEIVED, QUEUED, STARTED, FINISHED, CANCELLED, STOLEN;
	}

	private Status status;
	private final TaskViewCallback callback;
	private final JButton removeButton;
	private static final Color RECEIVED = Color.WHITE;
	private static final Color QUEUED = new Color(98, 173, 200);
	private static final Color STARTED = new Color(195, 200, 72);
	private static final Color FINISHED = new Color(118, 217, 101);
	private static final Color CANCELLED = new Color(255, 91, 90);
	private static final Color STOLEN = new Color(242, 130, 176);

	public TaskView(TaskViewCallback callback, String name) {
		this.callback = callback;
		this.removeButton = new JButton("X");
		this.removeButton.setPreferredSize(new Dimension(25, 20));
		this.removeButton.setBorder(BorderFactory.createEmptyBorder());	
		
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(200, 20));
		setBorder(new LineBorder(Color.BLACK));
		
		JLabel nameLabel = new JLabel(name);
		add(nameLabel, BorderLayout.CENTER);

		status = Status.RECEIVED;
		setBackground(RECEIVED);
		taskAdded(null);
		
		
	}

	@Override
	public void taskAdded(TaskId taskId) {
		status = Status.QUEUED;
		setBackground(QUEUED);
	}

	@Override
	public void taskStarted(TaskId taskId) {
		status = Status.STARTED;
		setBackground(STARTED);
	}

	@Override
	public void taskFinished(TaskId taskId) {
		status = Status.FINISHED;
		setBackground(FINISHED);
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TaskView.this.callback.taskRemoved(TaskView.this);
			}
		});
		add(removeButton, BorderLayout.EAST);
	}

	@Override
	public void taskCancelled(TaskId taskId) {
		status = Status.CANCELLED;
		setBackground(CANCELLED);
		JButton removeButton = new JButton("X");
		removeButton.setPreferredSize(new Dimension(25, 20));
		removeButton.setBorder(BorderFactory.createEmptyBorder());
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TaskView.this.callback.taskRemoved(TaskView.this);
			}
		});
		add(removeButton, BorderLayout.EAST);
	}

	@Override
	public void taskStolen(TaskId taskId) {
		status = Status.STOLEN;
		setBackground(STOLEN);
		JButton removeButton = new JButton("X");
		removeButton.setPreferredSize(new Dimension(25, 20));
		removeButton.setBorder(BorderFactory.createEmptyBorder());
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TaskView.this.callback.taskRemoved(TaskView.this);
			}
		});
		add(removeButton, BorderLayout.EAST);
	}

	public interface TaskViewCallback {
		void taskRemoved(JPanel panel);
	
	}
}
