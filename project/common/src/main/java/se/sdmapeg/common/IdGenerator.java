package se.sdmapeg.common;

/**
 * Interface representing a generator of unique IDs.
 *
 * @param <I> the type of IDs generated by this IdGenerator.
 */
public interface IdGenerator<I extends Id> {
	/**
	 * Generates a unique new ID.
	 *
	 * @return the generated ID.
	 */
	I newId();
}