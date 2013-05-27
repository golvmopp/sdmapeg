package se.sdmapeg.worker.taskperformers;

import java.util.concurrent.ExecutionException;

public class IdleTaskPerformer {
	
	private IdleTaskPerformer(){
		throw new AssertionError(); //Prevent instantiation
	}
	
	public static String idle() throws ExecutionException {
		try {
			Thread.sleep(3*60*1000);
			return "Success!";
		} catch (InterruptedException e) {
			throw new ExecutionException(e);
		}
	}
}
