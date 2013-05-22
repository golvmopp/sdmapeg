package se.sdmapeg.common.tasks;

public final class FindNextIntTask implements Task<Integer> {
	private static final long serialVersionUID = 5013770542226098887L;
	private int start;
	private String name;

	private FindNextIntTask(int start, String name) {
		this.start = start;
		this.name = name;
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

	public String getName() {
		return name;
	}

	public static FindNextIntTask newNextIntTask(int start, String name) {
		return new FindNextIntTask(start, name);
	}
}
