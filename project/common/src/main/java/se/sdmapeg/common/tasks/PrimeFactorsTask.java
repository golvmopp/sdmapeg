package se.sdmapeg.common.tasks;

import java.util.List;

public final class PrimeFactorsTask implements Task<List<Long>> {
	private long number;
	private String name;
	
	private static final long serialVersionUID = -4992273066048325517L;
	@SuppressWarnings("unchecked")
	private static final Class<List<Long>> RESULT_TYPE = (Class<List<Long>>) (Class<?>) List.class;

	
	private PrimeFactorsTask(long number, String name) {
		this.number = number;
		this.name = name;
	}
	
	public long getNumber() {
		return number;
	}
	
	@Override
	public Result<List<Long>> perform(TaskPerformer taskPerformer) {
		return taskPerformer.findPrimeFactors(this);
	}

	@Override
	public Class<List<Long>> resultType() {
		return RESULT_TYPE;
	}

	public String getName() {
		return name;
	}

	public static PrimeFactorsTask newPrimeFactorTask(long number, String name) {
		return new PrimeFactorsTask(number, name);
	}
}
