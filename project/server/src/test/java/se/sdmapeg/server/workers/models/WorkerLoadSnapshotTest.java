package se.sdmapeg.server.workers.models;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import static org.junit.Assert.*;
import org.junit.Test;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.server.test.PairIterator;
import se.sdmapeg.server.workers.callbacks.WorkerCallback;
import se.sdmapeg.server.workers.exceptions.TaskRejectedException;
import se.sdmapeg.serverworker.TaskId;

/**
 *
 * @author niclas
 */
public class WorkerLoadSnapshotTest {
	public WorkerLoadSnapshotTest() {
	}

	/**
	 * Test of getWorker method, of class WorkerLoadSnapshot.
	 */
	@Test
	public void testGetWorker() {
		InetSocketAddress address = InetSocketAddress.createUnresolved(
			"localhost", 1337);
		MockWorker worker = new MockWorker(address);
		WorkerLoadSnapshot instance = WorkerLoadSnapshot.newSnapshot(worker);
		assertSame(worker, instance.getWorker());
	}

	/**
	 * Test of ascendingComparator method, of class WorkerLoadSnapshot.
	 */
	@Test
	public void testAscendingComparator() {
		List<MockWorker> mockWorkers = createMockWorkers(10);
		setAscendingParallelWorkCapacity(mockWorkers);
		Collections.reverse(mockWorkers);
		List<WorkerLoadSnapshot> snapshots = createSnapshots(mockWorkers);
		Random rng = new Random(-1L);
		Collections.shuffle(snapshots, rng);
		Collections.sort(snapshots, WorkerLoadSnapshot.ascendingComparator());
		for (PairIterator.Pair<MockWorker, WorkerLoadSnapshot> pair
				: PairIterator.iterable(mockWorkers, snapshots)) {
			assertSame(pair.getLeft(), pair.getRight().getWorker());
		}
		List<MockWorker> unorderedList = new ArrayList<>(mockWorkers);
		Collections.shuffle(snapshots);
		setAscendingParallelWorkCapacity(unorderedList);
		Collections.shuffle(snapshots, rng);
		Collections.sort(snapshots, WorkerLoadSnapshot.ascendingComparator());
		for (PairIterator.Pair<MockWorker, WorkerLoadSnapshot> pair
				: PairIterator.iterable(mockWorkers, snapshots)) {
			assertSame(pair.getLeft(), pair.getRight().getWorker());
		}
	}

	/**
	 * Test of descendingComparator method, of class WorkerLoadSnapshot.
	 */
	@Test
	public void testDescendingComparator() {
		List<MockWorker> mockWorkers = createMockWorkers(10);
		setAscendingParallelWorkCapacity(mockWorkers);
		List<WorkerLoadSnapshot> snapshots = createSnapshots(mockWorkers);
		Random rng = new Random(-1L);
		Collections.shuffle(snapshots, rng);
		Collections.sort(snapshots, WorkerLoadSnapshot.descendingComparator());
		for (PairIterator.Pair<MockWorker, WorkerLoadSnapshot> pair
				: PairIterator.iterable(mockWorkers, snapshots)) {
			assertSame(pair.getLeft(), pair.getRight().getWorker());
		}
		List<MockWorker> unorderedList = new ArrayList<>(mockWorkers);
		Collections.shuffle(snapshots);
		setAscendingParallelWorkCapacity(unorderedList);
		Collections.shuffle(snapshots, rng);
		Collections.sort(snapshots, WorkerLoadSnapshot.descendingComparator());
		for (PairIterator.Pair<MockWorker, WorkerLoadSnapshot> pair
				: PairIterator.iterable(mockWorkers, snapshots)) {
			assertSame(pair.getLeft(), pair.getRight().getWorker());
		}
	}

	private static List<MockWorker> createMockWorkers(int n) {
		List<MockWorker> mockWorkers = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			mockWorkers.add(new MockWorker(null));
		}
		return mockWorkers;
	}

	private static void setAscendingParallelWorkCapacity(
			List<MockWorker> mockWorkers) {
		int capacity = 0;
		for (MockWorker mockWorker : mockWorkers) {
			capacity++;
			mockWorker.setParallellWorkCapacity(capacity);
		}
	}

	private List<WorkerLoadSnapshot> createSnapshots(
			List<MockWorker> mockWorkers) {
		List<WorkerLoadSnapshot> snapshots = new ArrayList<>(mockWorkers.size());
		for (MockWorker mockWorker : mockWorkers) {
			snapshots.add(WorkerLoadSnapshot.newSnapshot(mockWorker));
		}
		return snapshots;
	}
}
