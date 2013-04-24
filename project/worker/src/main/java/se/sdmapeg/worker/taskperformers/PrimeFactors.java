package se.sdmapeg.worker.taskperformers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public final class PrimeFactors {
	public static List<Long> findPrimeFactors(long number) throws ExecutionException {
		if (number<2) {
			throw new ExecutionException(new IllegalArgumentException("The number" +
					" to factor must be equal to or grater than two (number >= 2)."));
		}
		ArrayList<Long> primes = new ArrayList<>();
		for (long i = 2; number != 1; i++) {
			while (number % i == 0) {
				primes.add(Long.valueOf(i));
				number /= i;
			}
		}
		return primes;
	}
}
