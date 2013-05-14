package se.sdmapeg.common.tasks;

public final class FindNextPrimeTask implements Task<Long>{
	private static final long serialVersionUID = 4695270945487264731L;
	private long firstPrime;

	private FindNextPrimeTask(long prime) {
		this.firstPrime = prime;
	}
	
	public long getFirstPrime() {
		return firstPrime;
	}
	
	@Override
	public Result<Long> perform(TaskPerformer taskPerformer) {
		return taskPerformer.performFindNextPrimeTask(this);
	}

	@Override
	public Class<Long> resultType() {
		return Long.class;
	}
	
	public static FindNextPrimeTask newFindNextPrimeTask(long prime) {
		return new FindNextPrimeTask(prime);
	}
}
