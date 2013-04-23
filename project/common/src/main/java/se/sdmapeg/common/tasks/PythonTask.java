package se.sdmapeg.common.tasks;

public final class PythonTask implements Task<String> {
	private static final long serialVersionUID = -2539795844520824496L;
	private String pythonCode;

	private PythonTask(String pythonCode) {
		this.pythonCode = pythonCode;
	}

	public String getPythonCode() {
		return pythonCode;
	}

	@Override
	public Result<String> perform(TaskPerformer taskPerformer) {
		return taskPerformer.performPythonTask(this);
	}

	@Override
	public Class<String> resultType() {
		return String.class;
	}

	public static PythonTask newPythonTask(String pythonCode) {
		return new PythonTask(pythonCode);
	}
}
