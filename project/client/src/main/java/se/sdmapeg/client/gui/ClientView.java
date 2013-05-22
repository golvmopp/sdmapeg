package se.sdmapeg.client.gui;

import java.awt.GridLayout;

import javax.swing.JFrame;

import se.sdmapeg.client.models.Client;

/**
 * Class that handles the Client gui.
 */
public class ClientView extends JFrame {
	private final Client client;

	private ClientView(Client client) {
		this.client = client;

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLayout(new GridLayout(1, 2));

		setVisible(true);
	}

	@Override
	public void dispose() {
		client.shutDown();
		super.dispose();
	}

	public static ClientView newView(Client client) {
		return new ClientView(client);
	}
}
