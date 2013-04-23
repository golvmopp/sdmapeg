package se.sdmapeg.worker.taskperformers;

import java.util.ArrayList;
import java.util.List;

public final class PrimeFactors {
	public static List<Long> findPrimeFactors(long product) {
		ArrayList<Long> primes = new ArrayList<>();
		for (long i = 2; i * i < product; i++) {
			while (product % i == 0) {
				primes.add(Long.valueOf(i));
				product /= i;
			}
		}
		return primes;
	}
}
