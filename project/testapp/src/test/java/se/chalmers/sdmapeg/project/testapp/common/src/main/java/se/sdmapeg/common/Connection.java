package se.sdmapeg.common;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;

public interface Connection<S extends Message, R extends Message> extends Closeable {
	InetAddress getAddress();

	void send(S message);

	R receive();

	boolean isOpen();
}
