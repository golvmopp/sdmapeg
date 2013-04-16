package se.sdmapeg.client;

import se.sdmapeg.client.gui.ClientView;

import javax.swing.*;

public class Main {
	public static void main(String[] args) {
		String host = JOptionPane.showInputDialog("Adress:");
		int port = Integer.parseInt(JOptionPane.showInputDialog("Port:"));
		ClientImpl.newClientImp(ClientView.newView(), host, port);
	}
}
