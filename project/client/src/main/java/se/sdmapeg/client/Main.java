package se.sdmapeg.client;

import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sdmapeg.client.gui.ClientView;
import se.sdmapeg.common.communication.CommunicationException;

public class Main {
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		String host = JOptionPane.showInputDialog("Address:");
		ClientView view = ClientView.newView();
		try {
			ClientImpl.newClientImp(view, host).start();
		} catch (CommunicationException e) {
			JOptionPane.showMessageDialog(null, "Could not connect to server.");
			view.dispose();
		}
	}
}
