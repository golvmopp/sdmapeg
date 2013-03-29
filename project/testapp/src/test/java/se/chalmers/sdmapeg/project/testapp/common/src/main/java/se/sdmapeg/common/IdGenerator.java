package se.sdmapeg.common;

public interface IdGenerator<I extends Id> {
	I newId();
}