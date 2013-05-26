package se.sdmapeg.worker.taskperformers;

import java.util.concurrent.ExecutionException;

/**
 * A simple task for finding the next integer, given an integer to start from. 
 * 
 * @author Golvmopp
 *
 */
public class FindNextInteger {
	/**
	 * 
	 * 
	 * @param The value to increment. 
	 * @return The incremented value.
	 * @throws ExecutionException If the start value equals the maximum Integer value. 
	 */
	public static int findNextInteger(int start) throws ExecutionException {
		if (start==Integer.MAX_VALUE) {
			throw new ExecutionException(new ArithmeticException("Integer overflow"));
		}
		return start+1;
	}
}
