package se.sdmapeg.common.tasks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimpleListResult<R extends Serializable> implements Result<List<R>> {
	private static final long serialVersionUID = 32757656275068214L;
	private final ArrayList<R> list;
	
	private SimpleListResult(List<R> list) {
		this.list = new ArrayList<>(list);
	}

	@Override
	public List<R> get() {
		return Collections.unmodifiableList(list);
	}
	
	public static <R extends Serializable> SimpleListResult<R> newSimpleListResult(List<R> list) {
		return new SimpleListResult<>(list);
	}

}
