package se.sdmapeg.serverworker.communication;

/**
 * 
 */
public class WorkerIdentification implements WorkerToServerMessage {
	private final int parallelWorkCapacity;

	public WorkerIdentification(int parallelWorkCapacity) {
		this.parallelWorkCapacity = parallelWorkCapacity;
	}

	@Override
	public <T> T accept(Handler<T> handler) {
		return handler.handle(this);
	}

	public int getParallelWorkCapacity() {
		return parallelWorkCapacity;
	}
}
