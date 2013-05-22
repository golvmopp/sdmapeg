package se.sdmapeg.common.tasks;

public final class FindNextPrimeTask implements Task<Long>{
	private static final long serialVersionUID = 4695270945487264731L;
	private long firstPrime;
	private String name;

	private FindNextPrimeTask(long firstPrime, String name) {
		this.firstPrime = firstPrime;
		this.name = name;
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

	public String getName() {
		return name;
	}

	public static FindNextPrimeTask newFindNextPrimeTask(long prime, String name) {
		return new FindNextPrimeTask(prime, name);
	}
}
