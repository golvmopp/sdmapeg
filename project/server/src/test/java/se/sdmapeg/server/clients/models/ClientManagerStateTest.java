package se.sdmapeg.server.clients.models;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author niclas
 */
public class ClientManagerStateTest {
	public ClientManagerStateTest() {
	}

	/**
	 * Test of values method, of class ClientManagerState.
	 */
	@Test
	public void testValues() {
		ClientManagerState[] expResult = {ClientManagerState.CREATED,
										  ClientManagerState.STARTED,
										  ClientManagerState.STOPPED};
		ClientManagerState[] result = ClientManagerState.values();
		assertArrayEquals(expResult, result);
	}

	/**
	 * Test of valueOf method, of class ClientManagerState.
	 */
	@Test
	public void testValueOf() {
		testValueOf("CREATED", ClientManagerState.CREATED);
		testValueOf("STARTED", ClientManagerState.STARTED);
		testValueOf("STOPPED", ClientManagerState.STOPPED);
		try {
			testValueOf("wrong", null);
			fail("Expected an IllegalArgumentException");
		} catch (IllegalArgumentException ex) {
			// success
		}
	}

	private void testValueOf(String name, ClientManagerState expResult) {
		ClientManagerState result = ClientManagerState.valueOf(name);
		assertEquals(expResult, result);
	}
}
