package se.sdmapeg.client;

import javax.swing.JOptionPane;
import se.sdmapeg.client.gui.controllers.ClientController;
import se.sdmapeg.client.models.Client;
import se.sdmapeg.client.models.ClientImpl;
import se.sdmapeg.common.communication.CommunicationException;

public final class Main {
	private Main() {
		// Prevent instantiation
		throw new AssertionError();
	}
	
	public static void main(String[] args) {
		String host = JOptionPane.showInputDialog("Address:", "server.sdmapeg.se");
		Client client;
		try {
			client = ClientImpl.newClientImp(host);
		} catch (CommunicationException e) {
			JOptionPane.showMessageDialog(null, "Could not connect to server.");
			return;
		}
		ClientController.newClientController(client, host);
	}
}
