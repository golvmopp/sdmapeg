package se.sdmapeg.worker.taskperformers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * A task finding the prime factors of a given number. 
 * @author Golvmopp
 *
 */
public final class PrimeFactors {
	
	/**
	 * 
	 * @param The number to factor
	 * @return A list of the prime factors of the parameter. 
	 * @throws ExecutionException Whenever the input is less than 2. 
	 */
	public static List<Long> findPrimeFactors(long number) throws ExecutionException {
		if (number<2) {
			throw new ExecutionException(new IllegalArgumentException("The number" +
					" to factor must be equal to or greater than two (number >= 2)."));
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
