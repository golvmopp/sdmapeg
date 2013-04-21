package se.sdmapeg.client.gui;

import java.awt.*;

import javax.swing.JFrame;
import javax.swing.JPanel;

import se.sdmapeg.client.Client;

public class TaskListView extends JFrame {
	private final Client client;
	
	private TaskListView(Client client){
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.client = client;

		setLayout(new BorderLayout());
		JPanel centerList = new JPanel(new BorderLayout());
		add(centerList, BorderLayout.CENTER);

		JPanel connectionBar = new JPanel(new FlowLayout());
		add(connectionBar, BorderLayout.SOUTH);


	}

	@Override
	public void dispose(){
		client.shutDown();
		super.dispose();
	}

	public static TaskListView newTaskListView(Client client) {
		return new TaskListView(client);
	}
}
