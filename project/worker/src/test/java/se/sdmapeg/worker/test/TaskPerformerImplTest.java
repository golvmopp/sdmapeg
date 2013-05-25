package se.sdmapeg.worker.test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import se.sdmapeg.common.tasks.FindNextIntTask;
import se.sdmapeg.common.tasks.FindNextPrimeTask;
import se.sdmapeg.common.tasks.PrimeFactorsTask;
import se.sdmapeg.common.tasks.PythonTask;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.worker.models.TaskPerformerImpl;

public class TaskPerformerImplTest {

	@Test
	public void testPerformFindNextIntTask() {
		FindNextIntTask testTask = FindNextIntTask.newNextIntTask(0, "Test");
		TaskPerformerImpl instance = new TaskPerformerImpl();
		Result<Integer> result = instance.performFindNextIntTask(testTask);
		try {
			assertEquals(Integer.valueOf(1), result.get());
		} catch (ExecutionException ex) {
			fail();
		}
	}
	
	@Test
	public void testPerformFindNextIntTaskFailedTask() {
		FindNextIntTask testTask = FindNextIntTask.newNextIntTask(Integer.MAX_VALUE, "Test");
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
		FindNextPrimeTask testTask = FindNextPrimeTask.newFindNextPrimeTask(7, "Test");
		TaskPerformerImpl instance = new TaskPerformerImpl();
		Result<Long> result = instance.performFindNextPrimeTask(testTask);
		try {
			assertEquals(Long.valueOf(11), result.get());
		} catch (ExecutionException ex) {
			fail();
		}
	}
	
	@Test
	public void testPerformFindNextPrimeTaskFailedTask() {
		FindNextPrimeTask testTask = FindNextPrimeTask.newFindNextPrimeTask(Long.MAX_VALUE, "Test");
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
	public void testPerformPythonTask() {
		String pythonCode = "result=2";
		PythonTask testTask = PythonTask.newPythonTask(pythonCode, "Test");
		TaskPerformerImpl instance = new TaskPerformerImpl();
		Result<String> result = instance.performPythonTask(testTask);
		try {
			assertEquals("2", result.get());
		} catch (ExecutionException ex) {
			fail();
		}
	}
	
	@Test
	public void testPerformPythonTaskFailedTask() {
		String pythonCode = "result = ";
		PythonTask testTask = PythonTask.newPythonTask(pythonCode, "Test");
		TaskPerformerImpl instance = new TaskPerformerImpl();
		Result<String> result = instance.performPythonTask(testTask);
		try {
			result.get();
			fail();
		} catch (ExecutionException ex) {
			// Success!
		}
	}
	
	@Test
	public void testFindPrimeFactors() {
		PrimeFactorsTask testTask = PrimeFactorsTask.newPrimeFactorTask(10, "Test");
		TaskPerformerImpl instance = new TaskPerformerImpl();
		Result<List<Long>> result = instance.findPrimeFactors(testTask);
		try {
			assertEquals(Arrays.asList(2L,5L), result.get());
		} catch (ExecutionException ex) {
			fail();
		}
	}
	
	@Test
	public void testFindPrimeFactorsFailedTask() {
		PrimeFactorsTask testTask = PrimeFactorsTask.newPrimeFactorTask(-12, "Test");
		TaskPerformerImpl instance = new TaskPerformerImpl();
		Result<List<Long>> result = instance.findPrimeFactors(testTask);
		try {
			result.get();
			fail();
		} catch (ExecutionException ex) {
			// Success!
		}
	}
}
