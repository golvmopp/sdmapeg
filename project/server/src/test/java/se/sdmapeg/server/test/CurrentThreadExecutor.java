package se.sdmapeg.server.test;

import java.util.concurrent.Executor;

/**
 *
 * @author niclas
 */
public final class CurrentThreadExecutor implements Executor {

	@Override
	public void execute(Runnable command) {
		command.run();
	}
	
}
