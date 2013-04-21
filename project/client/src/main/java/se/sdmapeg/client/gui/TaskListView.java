package se.sdmapeg.client.gui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import se.sdmapeg.client.Client;

public class TaskListView extends JFrame {
	
	private final Client client;
	
	public TaskListView(Client client){
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.client = client;
		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel centerList = new JPanel(new BorderLayout());
		this.add(mainPanel);
	}

	@Override
	public void dispose(){
		client.shutDown();
		super.dispose();
	}

}
