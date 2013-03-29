package se.sdmapeg.server;

import se.sdmapeg.common.Connection;
import se.sdmapeg.common.Message;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketException;

public interface ConnectionHandler<S extends Message, R extends Message> extends Closeable {
	Connection<S, R> accept() throws IOException, SocketException;
	boolean isOpen();
}
