package se.sdmapeg.client.gui.tasks.PythonTask;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PythonTask extends JPanel {

	public PythonTask() {
		setLayout(new GridLayout(2, 1, 30, 10));

		JButton write = new JButton("Create");
		write.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//To change body of implemented methods use File | Settings | File Templates.
			}
		});
		add(write);

		JButton load = new JButton("Load");
		load.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//To change body of implemented methods use File | Settings | File Templates.
			}
		});
		add(load);
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("Hurr Durr");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new PythonTask());
		frame.pack();
		frame.setVisible(true);
	}
}
