package se.sdmapeg.serverworker.communication;

/**
 * Message containing relevant information for identifying a Worker.
 */
public interface WorkerIdentificationMessage extends WorkerToServerMessage {
	@Override
	<T> T accept(Handler<T> handler);

	/**
	 * Returns the number of tasks that the worker identifying itself is capable
	 * of performing in parallel.
	 *
	 * @return	the number of tasks that the worker identifying itself is
	 *			capable of performing in parallel
	 */
	int getParallelWorkCapacity();
}
