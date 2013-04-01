package se.sdmapeg.common;

import java.io.Serializable;

public interface Task<R> extends Serializable {
	<T> T accept(TaskVisitor<T> visitor);

	Class<R> resultType();
}
