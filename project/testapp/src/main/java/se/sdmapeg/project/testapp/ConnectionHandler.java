package se.sdmapeg.project.testapp;

/**
 *
 * @author niclas
 */
public interface ConnectionHandler {

	void handle(Connection<ServerMessage, ClientMessage> connection);

	void serverShutdown();
}
