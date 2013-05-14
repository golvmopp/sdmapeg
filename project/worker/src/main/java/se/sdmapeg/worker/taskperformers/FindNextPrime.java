package se.sdmapeg.worker.taskperformers;

import java.util.concurrent.ExecutionException;

public final class FindNextPrime {
	/**
	 * @param prime A prime number
	 * @return next prime if prime is prime
	 * @throws ExecutionException if prime isn't prime, or not greater than 0.
	 */
	public static long findNextPrime(long prime) throws ExecutionException {
		if (prime <= 0) {
			throw new ExecutionException(new ArithmeticException("Starting prime must be higher than 0"));
		}
		if (!isPrime(prime)) {
			throw new ExecutionException(new ArithmeticException("Given long wasn't prime."));
		}
		return nextPrime(prime);
	}

	private static long nextPrime(long prime) {
		prime++;
		while (!isPrime(prime)) {
			prime++;
		}
		return prime;
	}

	private static boolean isPrime(long prime) {
		for (long i = 2; i < Math.sqrt(prime); i++) {
			if (prime % i == 0) {
				return false;
			}
		}
		return true;
	}
}
