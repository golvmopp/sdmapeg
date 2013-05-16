package se.sdmapeg.worker.taskperformers;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import static org.junit.Assert.*;

import org.junit.Test;

public class FindNextPrimeTest {
	private final int[] primes = generatePrimes(2000000);

	@Test
	public void testFindNextPrime() throws ExecutionException {
		for (int i = 0; i < primes.length - 1; i++) {
			assertEquals(primes[i+1], FindNextPrime.findNextPrime(primes[i]));
		}
	}

	@Test(expected = ExecutionException.class)
	public void testFindNextPrimeLowerBound() throws Exception {
		FindNextPrime.findNextPrime(-1);
	}

	private static int[] generatePrimes(int max) {
		/*
		 * Code borrowed from  http://stackoverflow.com/questions/586284/
		 * 2013-05-14
		 */
	    boolean[] isComposite = new boolean[max + 1];
	    for (int i = 2; i * i <= max; i++) {
	        if (!isComposite [i]) {
	            for (int j = i; i * j <= max; j++) {
	                isComposite [i*j] = true;
	            }
	        }
	    }
	    int numPrimes = 0;
	    for (int i = 2; i <= max; i++) {
	        if (!isComposite [i]) numPrimes++;
	    }
	    int [] primes = new int [numPrimes];
	    int index = 0;
	    for (int i = 2; i <= max; i++) {
	        if (!isComposite [i]) primes [index++] = i;
	    }
	    return primes;
	}
}