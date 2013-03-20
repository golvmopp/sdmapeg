package se.chalmers.sdmapeg.project.testapp;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Hello world!
 *
 */
public class App {
	private static final LoadingCache<String, String> CACHE = CacheBuilder.newBuilder().maximumSize(1).build(new Loader());
	
	public static void main(String[] args) {
	    System.out.println("Hej jag heter golvmopp");
	    System.out.println("Hej jag heter Trivoc");
	    
	    int i = 0;
	    while(i < 10) {
		System.out.println(CACHE.getUnchecked("Hello World!"));
		i++;
	    }
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
