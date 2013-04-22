package se.sdmapeg.worker.taskperformers;

import java.util.concurrent.ExecutionException;

public class FindNextInteger {
	public static int findNextInteger(int start) throws ExecutionException {
		if (start==Integer.MAX_VALUE) {
			throw new ExecutionException(new ArithmeticException("Integer overflow"));
		}
		return start+1;
	}
}
