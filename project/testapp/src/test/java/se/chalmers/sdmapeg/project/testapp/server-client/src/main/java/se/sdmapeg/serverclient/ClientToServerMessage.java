package se.sdmapeg.serverclient;

import se.sdmapeg.common.Message;

public interface ClientToServerMessage extends Message {
	<T> T accept(Visitor<T> visitor);

	interface Visitor<T> {

	}
}
