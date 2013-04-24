package se.sdmapeg.common.tasks;

import java.io.Serializable;

/**
 * Simple implementation of a result.
 */
public final class SimpleResult<R extends Serializable> implements Result<R> {
	private static final long serialVersionUID = -1781557764231472514L;
	private final R result;

	private SimpleResult(R result) {
		this.result = result;
	}

	@Override
	public R get() {
		return result;
	}

	@Override
	public String toString() {
		return "Result [result=" + result + "]";
	}
	
	/**
	 * Creates a new SimpleResult with the specified result.
	 */
	public static <R extends Serializable> Result<R> newSimpleResult(R result) {
		return new SimpleResult<>(result);
	}
}
