package se.sdmapeg.common;

import java.io.Serializable;

public interface Id extends Serializable {
	boolean equals(Object o);
	int hashCode();
}
