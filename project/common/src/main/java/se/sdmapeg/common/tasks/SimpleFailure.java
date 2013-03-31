package se.sdmapeg.common.tasks;

import java.util.concurrent.ExecutionException;

/**
 * Simple implementation of a task failure.
 */
public class SimpleFailure<R> implements Result<R> {
    private static final long serialVersionUID = -1791290556958512522L;
    private final ExecutionException exception;

    /**
     * Creates a new SimpleFailure with the specified exception.
     */
    public SimpleFailure(ExecutionException exception) {
	this.exception = exception;
    }

    @Override
    public String toString() {
	return "Failure [exception=" + exception + "]";
    }

    public R get() throws ExecutionException {
	throw exception;
    }
}
