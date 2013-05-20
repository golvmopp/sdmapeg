package se.sdmapeg.worker;

import static org.junit.Assert.*;

import java.util.concurrent.ExecutionException;

import org.junit.Test;

import se.sdmapeg.common.tasks.FindNextIntTask;
import se.sdmapeg.common.tasks.FindNextPrimeTask;
import se.sdmapeg.common.tasks.Result;

public class TaskPerformerImplTest {

	@Test
	public void testPerformFindNextIntTask() {
		FindNextIntTask testTask = FindNextIntTask.newNextIntTask(0);
		TaskPerformerImpl instance = new TaskPerformerImpl();
		Result<Integer> result = instance.performFindNextIntTask(testTask);
		try {
			assertEquals(Integer.valueOf(1), result.get());
		} catch (ExecutionException ex) {
			fail();
		}
	}
	
	@Test
	public void testPerformFindNextIntTaskUpperBound() {
		FindNextIntTask testTask = FindNextIntTask.newNextIntTask(Integer.MAX_VALUE);
		TaskPerformerImpl instance = new TaskPerformerImpl();
		Result<Integer> result = instance.performFindNextIntTask(testTask);
		try {
			result.get();
			fail();
		} catch (ExecutionException ex) {
			// Success!
		}
	}
	
	@Test
	public void testPerformFindNextPrimeTask() {
		FindNextPrimeTask testTask = FindNextPrimeTask.newFindNextPrimeTask(7);
		TaskPerformerImpl instance = new TaskPerformerImpl();
		Result<Long> result = instance.performFindNextPrimeTask(testTask);
		try {
			assertEquals(Long.valueOf(11), result.get());
		} catch (ExecutionException ex) {
			fail();
		}
	}
	
	@Test
	public void testPerformFindNextPrimeTaskTooLargeNumber() {
		FindNextPrimeTask testTask = FindNextPrimeTask.newFindNextPrimeTask(Long.MAX_VALUE);
		TaskPerformerImpl instance = new TaskPerformerImpl();
		Result<Long> result = instance.performFindNextPrimeTask(testTask);
		try {
			result.get();
			fail();
		} catch (ExecutionException ex) {
			// Success!
		}
	}
	
	@Test
	public void testPerformFindNextPrimeTaskNegativeNumber() {
		FindNextPrimeTask testTask = FindNextPrimeTask.newFindNextPrimeTask(-12);
		TaskPerformerImpl instance = new TaskPerformerImpl();
		Result<Long> result = instance.performFindNextPrimeTask(testTask);
		try {
			result.get();
			fail();
		} catch (ExecutionException ex) {
			// Success!
		}
	}
}
