package se.sdmapeg.common;

import java.io.Serializable;

/**
 * Interface representing an ID. Classes implementing this interface <b>must</b>
 * override the equals and hashCode methods in Object (or perform instance
 * control, including when deserializing objects), as to properly represent a
 * unique identity.
 */
public interface Id extends Serializable {
	@Override
	boolean equals(Object o);

	@Override
	int hashCode();
}
