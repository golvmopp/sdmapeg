package se.sdmapeg.worker.taskperformers;

import java.util.concurrent.ExecutionException;

import org.junit.Test;

public class PrimeFactorsTest {

	@Test
	public void testPrimeFactors() throws Exception {
		// TODO Implement test.
	}
	
	@Test(expected=ExecutionException.class)
	public void testPrimeFactorsLowerBound() throws Exception {
		PrimeFactors.findPrimeFactors(-10);
	}

}
