package se.sdmapeg.client.gui.taskmanagement;

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

	private String name;

	private TaskModel(String name, ClientTaskId clientTaskId) {
		this.timeStamp = Calendar.getInstance();
		this.state = TaskState.CREATED;
		this.name = name;
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
		return name;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public void setState(TaskState state) {
		this.state = state;
	}

	public static TaskModel newTaskModel(String typeName, ClientTaskId clientTaskId) {
		return new TaskModel(typeName, clientTaskId);
	}
}
