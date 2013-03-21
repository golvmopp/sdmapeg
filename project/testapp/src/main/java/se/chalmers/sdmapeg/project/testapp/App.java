package se.chalmers.sdmapeg.project.testapp;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import javax.swing.JOptionPane;

/**
 * Hello world!
 *
 */
public class App {
	private static final LoadingCache<String, String> CACHE = CacheBuilder
			.newBuilder().maximumSize(1).build(new Loader());

	public static void main(String[] args) {
		JOptionPane.showMessageDialog(null, "Hej jag heter Trivoc");
		JOptionPane.showMessageDialog(null, "Hej du heter Rascal");

		int i = 0;
		while (i < 10) {
			JOptionPane.showMessageDialog(null, CACHE.getUnchecked(
					i + ": Hello World!"));
			i++;
		}

		JOptionPane.showMessageDialog(null, "Hej jag heter golvmopp, ändrat");
		JOptionPane.showMessageDialog(null, "Great Success!");
	}

	private static class Loader extends CacheLoader<String, String> {
		public Loader() {
		}

		@Override
		public String load(String arg0) {
			return arg0.toLowerCase();
		}
	}
}
