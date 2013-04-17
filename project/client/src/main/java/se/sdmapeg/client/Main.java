package se.sdmapeg.client;

import se.sdmapeg.client.gui.ClientView;
import se.sdmapeg.common.communication.CommunicationException;

import javax.swing.*;

public class Main {
	public static void main(String[] args) {
		String host = JOptionPane.showInputDialog("Adress:");
		int port = Integer.parseInt(JOptionPane.showInputDialog("Port:"));
		ClientView view = ClientView.newView();
		try {
			ClientImpl.newClientImp(view, host, port);
		} catch (CommunicationException e) {
			JOptionPane.showMessageDialog(null, "Could not connect to server.");
			view.dispose();
		}
	}
}
