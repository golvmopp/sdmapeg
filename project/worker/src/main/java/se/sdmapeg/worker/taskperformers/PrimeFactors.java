package se.sdmapeg.worker.taskperformers;

import java.util.ArrayList;
import java.util.List;

public final class PrimeFactors {
	public static void main(String args[]) {
		long n = (long) Integer.MAX_VALUE * Integer.MAX_VALUE + 1L;
		long start = System.nanoTime();
		List<Long> result = findPrimeFactors(n);
		long end = System.nanoTime();
		System.out.println("The factors of " + n + " are " + result + " in " +(end-start)/1000000 + "ms");
	}

	public static List<Long> findPrimeFactors(long product) {
		ArrayList<Long> primes = new ArrayList<>();
		for (long i = 2; product != 1; i++) {
			while (product % i == 0) {
				primes.add(Long.valueOf(i));
				product /= i;
			}
		}
		return primes;
	}
}
