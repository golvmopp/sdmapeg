package se.sdmapeg.worker.taskperformers;

import java.util.concurrent.ExecutionException;

public final class FindNextPrime {
	/**
	 * @param number A non-negative integer.
	 * @return Closest prime number that is larger than number.
	 * @throws IllegalArgumentException If number is negative.
	 */
	public static long findNextPrime(long number) throws ExecutionException {
		if (number < 0) {
			throw new ExecutionException(new IllegalArgumentException("Starting number must not be negative"));
		}
		return nextPrime(number);
	}

	private static long nextPrime(long number) {
		number++;
		while (!isPrime(number)) {
			number++;
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
