package se.sdmapeg.server.workers.models;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author niclas
 */
public class WorkerCoordinatorStateTest {
	public WorkerCoordinatorStateTest() {
	}

	/**
	 * Test of values method, of class WorkerCoordinatorState.
	 */
	@Test
	public void testValues() {
		WorkerCoordinatorState[] expResult = {WorkerCoordinatorState.CREATED,
										  WorkerCoordinatorState.STARTED,
										  WorkerCoordinatorState.STOPPED};
		WorkerCoordinatorState[] result = WorkerCoordinatorState.values();
		assertArrayEquals(expResult, result);
	}

	/**
	 * Test of valueOf method, of class WorkerCoordinatorState.
	 */
	@Test
	public void testValueOf() {
		testValueOf("CREATED", WorkerCoordinatorState.CREATED);
		testValueOf("STARTED", WorkerCoordinatorState.STARTED);
		testValueOf("STOPPED", WorkerCoordinatorState.STOPPED);
		try {
			testValueOf("wrong", null);
			fail("Expected an IllegalArgumentException");
		} catch (IllegalArgumentException ex) {
			// success
		}
	}

	private void testValueOf(String name, WorkerCoordinatorState expResult) {
		WorkerCoordinatorState result = WorkerCoordinatorState.valueOf(name);
		assertEquals(expResult, result);
	}
}
