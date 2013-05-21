package se.sdmapeg.server.clients.models;

import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.common.tasks.Task;
import se.sdmapeg.common.tasks.TaskPerformer;

/**
 *
 * @author niclas
 */
public final class MockTask implements Task<Void> {

	public MockTask() {
	}

	@Override
	public Result<Void> perform(TaskPerformer taskPerformer) {
		return null;
	}

	@Override
	public Class<Void> resultType() {
		return Void.TYPE;
	}
	
}
