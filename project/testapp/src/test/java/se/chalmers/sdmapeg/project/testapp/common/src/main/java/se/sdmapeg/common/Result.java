package se.sdmapeg.common;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;

public interface Result<R> extends Serializable {
	R get() throws ExecutionException;

}
