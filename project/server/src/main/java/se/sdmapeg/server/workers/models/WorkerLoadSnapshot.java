package se.sdmapeg.server.workers.models;

import java.util.Collections;
import java.util.Comparator;

/**
 * Immutable class used for sorting (mutable) workers according to their
 * load at some point in time. Since Workers are mutable and may be modified
 * by other threads at any point in time, attempting to sort them using a
 * regular comparator might lead to unspecified behaviour in the sorting
 * algorithm if another thread changes them during the sort.
 * <p />
 * This class provides an immutable snapshot of the state of the Worker,
 * which can safely be sorted without risk of other threads modifying it in
 * the process. Due to this immutable nature, it is possible that a list of
 * LoadSnapshots will be outdated after being sorted. This class is intended
 * for use in situations where the risk of having an outdated list is an
 * acceptable tradeoff.
 */
final class WorkerLoadSnapshot {
	private static final Comparator<WorkerLoadSnapshot> ASCENDING =
		new LoadComparator();
	private static final Comparator<WorkerLoadSnapshot> DESCENDING =
		Collections.reverseOrder(ASCENDING);
	private final Worker worker;
	private final int load;

	private WorkerLoadSnapshot(Worker worker) {
		this.worker = worker;
		this.load = worker.getLoad();
	}

	/**
	 * Returns the Worker that this WorkerLoadSnapshot represents.
	 *
	 * @return the Worker that this WorkerLoadSnapshot represents
	 */
	public Worker getWorker() {
		return worker;
	}

	private int getLoad() {
		return load;
	}

	/**
	 * Returns a Comparator that imposes an ascending order on
	 * WorkerLoadSnapshots according to their load.
	 *
	 * @return	a Comparator that imposes an ascending order on
	 *			WorkerLoadSnapshots
	 */
	public static Comparator<WorkerLoadSnapshot> ascendingComparator() {
		return ASCENDING;
	}


	/**
	 * Returns a Comparator that imposes a descending order on
	 * WorkerLoadSnapshots according to their load.
	 *
	 * @return	a Comparator that imposes an descending order on
	 *			WorkerLoadSnapshots
	 */
	public static Comparator<WorkerLoadSnapshot> descendingComparator() {
		return DESCENDING;
	}

	private static final class LoadComparator
			implements Comparator<WorkerLoadSnapshot> {
		@Override
		public int compare(WorkerLoadSnapshot o1, WorkerLoadSnapshot o2) {
			return Integer.compare(o1.getLoad(), o2.getLoad());
		}
	}

	/**
	 * Returns a new snapshot of the specified Worker's current load state.
	 *
	 * @param worker the Worker to provide a snapshot of
	 * @return a new snapshot of the Worker's load
	 */
	public static WorkerLoadSnapshot newSnapshot(Worker worker) {
		return new WorkerLoadSnapshot(worker);
	}
}
