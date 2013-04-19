package se.sdmapeg.common.tasks;

import java.util.concurrent.ExecutionException;

public class NextIntTask implements Task<Integer>{

	private static final long serialVersionUID = 5013770542226098887L;
	private int start;

	@Override
	public Result<Integer> perform(TaskPerformer taskPerformer)
			throws ExecutionException {
		return taskPerformer.performFindNextIntTask();
	}

	public int getStart() {
		return start;
	}
	@Override
	public Class<Integer> resultType() {
		return Integer.class;
	}

}
