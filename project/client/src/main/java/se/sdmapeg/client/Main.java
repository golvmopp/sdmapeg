package se.sdmapeg.client;

import se.sdmapeg.client.GUI.ClientView;

import javax.swing.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main {
	public static void main(String[] args) {
		String host = JOptionPane.showInputDialog("Adress:");
		int port = Integer.parseInt(JOptionPane.showInputDialog("Port:"));
		ClientImpl.newClientImp(ClientView.newView(), host, port);
	}
}
