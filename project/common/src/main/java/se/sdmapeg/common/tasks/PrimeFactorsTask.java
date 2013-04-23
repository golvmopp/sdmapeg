package se.sdmapeg.common.tasks;

import java.util.List;

public class PrimeFactorsTask implements Task<List<Long>> {
	private long number;
	
	private static final long serialVersionUID = -4992273066048325517L;
	@SuppressWarnings("unchecked")
	private static final Class<List<Long>> RESULT_TYPE = (Class<List<Long>>) (Class<?>) List.class;

	
	private PrimeFactorsTask(long number) {
		this.number = number;
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
	
	public static PrimeFactorsTask newPrimeFactorTask(long number) {
		return new PrimeFactorsTask(number);
	}
}
