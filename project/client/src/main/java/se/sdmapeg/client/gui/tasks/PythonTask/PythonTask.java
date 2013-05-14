package se.sdmapeg.client.gui.tasks.PythonTask;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: Oskar
 * Date: 2013-05-13
 * Time: 11:28
 * To change this template use File | Settings | File Templates.
 */
public class PythonTask extends JPanel {

	public static void main(String[] args) {
		JFrame frame = new JFrame("Hurr Durr");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new PythonTask());
		frame.pack();
		frame.setVisible(true);
	}
}
