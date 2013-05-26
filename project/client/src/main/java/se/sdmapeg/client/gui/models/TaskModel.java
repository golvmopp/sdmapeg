package se.sdmapeg.client.gui.models;

import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.serverclient.ClientTaskId;

import java.util.Calendar;

public class TaskModel {
	public enum TaskState {
		CREATED, SENT, COMPLETED, FAILED;
	}

	private TaskState state;
	private final ClientTaskId clientTaskId;
	private long startTime;
	private Calendar timeStamp;

	private Task<?> task;

	private TaskModel(Task<?> task, ClientTaskId clientTaskId) {
		this.timeStamp = Calendar.getInstance();
		this.state = TaskState.CREATED;
		this.task = task;
		this.clientTaskId = clientTaskId;
	}

	public TaskState getState() {
		return state;
	}

	public ClientTaskId getClientTaskId() {
		return clientTaskId;
	}

	public long getStartTime() {
		return startTime;
	}

	public Calendar getTimeStamp() {
		return timeStamp;
	}

	public String getName() {
		return task.getName();
	}

	public String getTypeName() {
		return task.getTypeName();
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public void setState(TaskState state) {
		this.state = state;
	}

	public static TaskModel newTaskModel(Task<?> task, ClientTaskId clientTaskId) {
		return new TaskModel(task, clientTaskId);
	}
}
