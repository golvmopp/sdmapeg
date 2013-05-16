package se.sdmapeg.client.gui;

import se.sdmapeg.common.TimeFormatter;
import se.sdmapeg.common.tasks.PythonTask;
import se.sdmapeg.common.tasks.Task;

public class TaskModel<T extends Task<?>> {
	private final TaskType type;
	private T task;
	private String name;
	private TimeFormatter createdTimestamp;
	private long editedTimestamp;
	private long sentTimestamp;
	private long resultTimestamp;

	private TaskModel(T task, TaskType type) {
		this.task = task;
		this.type = type;
	}

	/**
	 * @return the task
	 */
	public T getTask() {
		return task;
	}
	/**
	 * @return the type
	 */
	public TaskType getType() {
		return type;
	}
}
