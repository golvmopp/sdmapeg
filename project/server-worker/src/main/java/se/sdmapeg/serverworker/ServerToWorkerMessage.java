package se.sdmapeg.serverworker;

import se.sdmapeg.common.Message;

public interface ServerToWorkerMessage extends Message {
	<T> T accept(Visitor<T> visitor);

	interface Visitor<T> {

	}
}
