package se.sdmapeg.project.testapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Hello world!
 */
public class App {
	private static final Logger LOG = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) {
		ClientServerInteractionDemo.interact();
		PythonDemo.runInterpreter();
		LOG.info("Hello World!");
	}
}
