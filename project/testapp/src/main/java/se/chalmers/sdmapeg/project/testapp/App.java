package se.chalmers.sdmapeg.project.testapp;

import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
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
