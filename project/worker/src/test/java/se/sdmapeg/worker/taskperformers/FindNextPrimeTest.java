package se.sdmapeg.worker.taskperformers;

import static org.junit.Assert.*;

import java.util.concurrent.ExecutionException;

import org.junit.Test;

public class FindNextPrimeTest {

	@Test
	public void testFindNextPrime() throws ExecutionException {
		long input = 2147483629;
		long expected = Integer.MAX_VALUE;
		assertEquals(3, FindNextPrime.findNextPrime(2));
		assertEquals(43, FindNextPrime.findNextPrime(41));
		assertEquals(104729, FindNextPrime.findNextPrime(104723));
		assertEquals(expected, FindNextPrime.findNextPrime(input));
		
	}
	
	@Test(expected=ExecutionException.class)
	public void testFindNextPrimeLowerBound() throws Exception {
		FindNextPrime.findNextPrime(0);
	}
	
	@Test(expected=ExecutionException.class)
	public void testFindNextPrimeNoPrime() throws Exception {
		FindNextPrime.findNextPrime(120);
	}

}
