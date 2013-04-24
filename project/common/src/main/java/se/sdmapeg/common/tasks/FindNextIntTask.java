package se.sdmapeg.common.tasks;

public final class FindNextIntTask implements Task<Integer> {
	private static final long serialVersionUID = 5013770542226098887L;
	private int start;
	
	private FindNextIntTask(int start) {
		this.start = start;
	}
	
	public int getStart() {
		return start;
	}
	
	@Override
	public Result<Integer> perform(TaskPerformer taskPerformer) {
		return taskPerformer.performFindNextIntTask(this);
	}

	@Override
	public Class<Integer> resultType() {
		return Integer.class;
	}

	public static FindNextIntTask newNextIntTask(int start) {
		return new FindNextIntTask(start);
	}
}
