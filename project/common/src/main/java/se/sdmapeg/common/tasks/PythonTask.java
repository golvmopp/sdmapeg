package se.sdmapeg.common.tasks;

public final class PythonTask implements Task<String> {
	private static final long serialVersionUID = -2539795844520824496L;
	private String pythonCode;
	private String name;

	private PythonTask(String pythonCode, String name) {
		this.pythonCode = pythonCode;
		this.name = name;
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

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getTypeName() {
		return "Python Task";
	}

	public static PythonTask newPythonTask(String pythonCode, String name) {
		return new PythonTask(pythonCode, name);
	}
}
