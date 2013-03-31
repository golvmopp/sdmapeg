package se.sdmapeg.common.tasks;

import java.io.Serializable;


/**
 * Interface for representing a task.
 * @param <R> the type of the result expected by this task.
 */
public interface Task<R> extends Serializable {
	/**
	 * Accept method for the visitor pattern.
	 * @param <T> the return type of the visitor.
	 * @param visitor the visitor.
	 * @return the result of calling the visitor's visit method.
	 */
	<T> T accept(TaskVisitor<T> visitor);

	/**
	 * Returns the type of result expected by this task.
	 * @return the type of result expected by this task.
	 */
	Class<R> resultType();
}
