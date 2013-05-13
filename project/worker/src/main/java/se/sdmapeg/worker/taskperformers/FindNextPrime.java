package se.sdmapeg.worker.taskperformers;

import java.util.concurrent.ExecutionException;

public final class FindNextPrime {
	/**
	 * @param prime A prime number
	 * @return given long if it isn't prime, next prime if prime is prime
	 */
	public static long findNextPrime(long prime) throws ExecutionException {
		if (!isPrime(prime)) {
			return prime;
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
