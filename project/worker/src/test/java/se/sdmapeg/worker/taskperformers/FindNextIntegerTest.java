package se.sdmapeg.worker.taskperformers;

import static org.junit.Assert.*;

import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

public class FindNextIntegerTest {

	
	@Test
	public void testFindNextInteger() throws Exception {
		Random rng = new Random(-1L);
		for (int i = 0; i < 1000000; i++) {
			int input = rng.nextInt(Integer.MAX_VALUE);
			int expected = input+1;
			assertEquals(expected, FindNextInteger.findNextInteger(input));
		}
	}

	@Test(expected=ExecutionException.class)
	public void testFindNextIntegerUpperBound() throws Exception {
		int input = Integer.MAX_VALUE;
		FindNextInteger.findNextInteger(input);
	}
}