package se.sdmapeg.serverworker.communication;

/**
 * Message containing relevant information for identifying a Worker.
 */
public final class WorkerIdentification implements WorkerToServerMessage {
	private static final long serialVersionUID = -387238449857548L;
	private final int parallelWorkCapacity;

	private WorkerIdentification(int parallelWorkCapacity) {
		this.parallelWorkCapacity = parallelWorkCapacity;
	}

	@Override
	public <T> T accept(Handler<T> handler) {
		return handler.handle(this);
	}

	/**
	 * Returns the number of tasks that the worker identifying itself is capable
	 * of performing in parallel.
	 *
	 * @return	the number of tasks that the worker identifying itself is
	 *			capable of performing in parallel
	 */
	public int getParallelWorkCapacity() {
		return parallelWorkCapacity;
	}

	/**
	 * Creates a new worker identification message with the specified data.
	 *
	 * @param parallelWorkCapacity	the number of tasks that the worker
	 *								identifying itself is capable of
	 *								performing in parallel
	 * @return the created message
	 */
	public static WorkerToServerMessage newWorkerIdentification(
			int parallelWorkCapacity) {
		return new WorkerIdentification(parallelWorkCapacity);
	}
}
