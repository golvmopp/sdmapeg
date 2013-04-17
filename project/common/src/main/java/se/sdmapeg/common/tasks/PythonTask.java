package se.sdmapeg.common.tasks;

import java.util.concurrent.ExecutionException;

public class PythonTask implements Task {
	private String pythonCode;

	private PythonTask(String pythonCode) {
		this.pythonCode = pythonCode;
	}

	public String getPythonCode() {
		return pythonCode;
	}

	@Override
	public Result<String> perform(TaskPerformer taskPerformer) throws ExecutionException {
		return taskPerformer.performPythonTask(this);
	}

	@Override
	public Class resultType() {
		return String.class;
	}

	public static PythonTask newPythonTask(String pythonCode) {
		return new PythonTask(pythonCode);
	}
}
