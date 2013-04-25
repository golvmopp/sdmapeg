package se.sdmapeg.client.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.jdesktop.swingx.JXHyperlink;

import se.sdmapeg.client.Client;
import se.sdmapeg.client.ClientImpl;

public class TaskListView extends JFrame implements ActionListener {
	private final Client client;
	private final JPanel taskListView;
	private final JLabel connectionInfoLabel;
	private final JXHyperlink connectButton;
	
	public TaskListView(Client client){
		setPreferredSize(new Dimension(300, 500));
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.client = client;

		setLayout(new BorderLayout());
		JPanel proxyPanel = new JPanel();
		JPanel centerList = new JPanel(new BorderLayout());
		JLabel titleLabel = new JLabel("Tasks");
		add(titleLabel, BorderLayout.NORTH);
		add(centerList, BorderLayout.CENTER);
		
		taskListView = new JPanel(new GridLayout(0, 1));
		JScrollPane taskList = new JScrollPane();
		proxyPanel.add(taskListView);
		taskList.setViewportView(proxyPanel);
		taskList.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		taskList.setBorder(new LineBorder(Color.BLACK));
		centerList.add(taskList);
		
		JPanel buttonPanel = new JPanel(new GridLayout(1, 0));
		BottomButton clearButton = new BottomButton("Clear", true);
		BottomButton addButton = new BottomButton("Add");
		BottomButton sendButton = new BottomButton("Send");
		buttonPanel.add(clearButton);
		buttonPanel.add(addButton);
		buttonPanel.add(sendButton);
		addButton.addActionListener(this);
		centerList.add(buttonPanel, BorderLayout.SOUTH);
		
		JPanel connectionBar = new JPanel(new BorderLayout());
		add(connectionBar, BorderLayout.SOUTH);
		connectionInfoLabel = new JLabel("Connected to ...");
		connectButton = new JXHyperlink(); 
		connectButton.setText("Connect");  
		connectionBar.add(connectionInfoLabel, BorderLayout.WEST);
		connectionBar.add(connectButton, BorderLayout.EAST);	
	}

	@Override
	public void dispose(){
	//	client.shutDown();
		super.dispose();
	}
	
	public void addTask(String typeName){
		taskListView.add(new TaskPanel(typeName));
		SwingUtilities.getRoot(taskListView).validate();
	}
	
	public void addTask(String typeName, String taskName){
		taskListView.add(new TaskPanel(typeName, taskName));
	}
		
	
	//TODO: Remove this when done. Duh. 
	public static void main(String[] args){
		JFrame frame =  new TaskListView(null);
		frame.pack();
		frame.setVisible(true);
	}

	
	//TODO: Just for testing, this is to be moved. 
	@Override
	public void actionPerformed(ActionEvent e) {
		addTask("YES");
	}
	

	
}
