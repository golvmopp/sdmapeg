package se.sdmapeg.client;

import javax.swing.JOptionPane;

import com.sun.deploy.trace.LoggerTraceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sdmapeg.client.gui.ClientView;
import se.sdmapeg.common.communication.CommunicationException;

public class Main {
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		String host = JOptionPane.showInputDialog("Address:");
		String portString = JOptionPane.showInputDialog("Port:");
		while (!portString.matches("\\d+")) {
			JOptionPane.showMessageDialog(null, "Invalid port");
			portString = JOptionPane.showInputDialog("Port:");
		}
		int port = Integer.parseInt(portString);
		ClientView view = ClientView.newView();
		try {
			ClientImpl.newClientImp(view, host, port).receive();
		} catch (CommunicationException e) {
			JOptionPane.showMessageDialog(null, "Could not connect to server.");
			view.dispose();
		}
	}
}
