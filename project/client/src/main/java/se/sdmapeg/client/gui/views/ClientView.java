package se.sdmapeg.client.gui.views;

import java.awt.*;

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
		setTitle("SDMAPeG Client");
		setLayout(new BorderLayout(10, 0));

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
