package se.sdmapeg.worker.taskperformers;

import java.util.concurrent.ExecutionException;

public final class FindNextPrime {
	/**
	 * @param number A non-negative integer.
	 * @return Closest prime number that is larger than number.
	 * @throws IllegalArgumentException If number is negative or doesn't have a higher prime within Long boundaries.
	 */
	public static long findNextPrime(long number) throws ExecutionException {
		if (number < 0) {
			throw new ExecutionException(new IllegalArgumentException("Starting number must not be negative"));
		}
		return nextPrime(number);
	}

	private static long nextPrime(long number) throws ExecutionException {
		number++;
		if (number < 0) {
			throw new ExecutionException(new IllegalArgumentException("Starting number too high, Long overflow"));
		}
		while (!isPrime(number)) {
			number++;
			if (number < 0) {
				throw new ExecutionException(new IllegalArgumentException("Starting number too high, Long overflow"));
			}
		}
		return number;
	}

	private static boolean isPrime(long prime) {
		for (long i = 2; i <= Math.sqrt(prime); i++) {
			if (prime % i == 0) {
				return false;
			}
		}
		return true;
	}
}
