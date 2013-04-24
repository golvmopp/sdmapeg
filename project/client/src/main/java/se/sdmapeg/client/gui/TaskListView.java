package se.sdmapeg.client.gui;

import java.awt.*;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import se.sdmapeg.client.Client;

public class TaskListView extends JFrame {
	private final Client client;
	private final JPanel taskListView;
	
	public TaskListView(Client client){
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.client = client;

		setLayout(new BorderLayout());
		JPanel centerList = new JPanel(new BorderLayout());
		add(centerList, BorderLayout.CENTER);
		
		taskListView = new JPanel(new GridLayout(0, 1));
		JScrollPane taskList = new JScrollPane(taskListView);
		
		JPanel buttonPanel = new JPanel(new GridLayout(1, 0));
		BottomButton clearButton = new BottomButton("Clear", true);
		BottomButton addButton = new BottomButton("Add");
		BottomButton sendButton = new BottomButton("Send");
		buttonPanel.add(clearButton);
		buttonPanel.add(addButton);
		buttonPanel.add(sendButton);
		centerList.add(buttonPanel, BorderLayout.SOUTH);
		
		JPanel connectionBar = new JPanel(new FlowLayout());
		add(connectionBar, BorderLayout.SOUTH);
	}

	@Override
	public void dispose(){
		client.shutDown();
		super.dispose();
	}
	
	public void addTask(String typeName){
		taskListView.add(new TaskPanel(typeName));
	}
	
	public void addTask(String typeName, String taskName){
		taskListView.add(new TaskPanel(typeName, taskName));
	}
	
	
	
	
	//TODO: Remove this when done. Duh. 
	public static void main(String[] args){
		JFrame frame = new JFrame();
		frame.add(new TaskListView(null));
		frame.pack();
		frame.setVisible(true);
	}
	

}
