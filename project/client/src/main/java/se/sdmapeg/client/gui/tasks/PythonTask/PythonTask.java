package se.sdmapeg.client.gui.tasks.PythonTask;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PythonTask extends JPanel implements PythonEditor.Callback {

	public PythonTask() {
		setLayout(new BorderLayout());

		JPanel buttons = new JPanel(new GridLayout(2, 1, 10, 10));
		add(buttons, BorderLayout.NORTH);

		JButton write = new JButton("Create Python script");
		write.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new PythonEditor(PythonTask.this);
			}
		});
		buttons.add(write);

		JButton load = new JButton("Load Python script");
		load.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.showDialog(PythonTask.this, "Öppna");
				new PythonEditor(PythonTask.this, fileChooser.getSelectedFile());
			}
		});
		buttons.add(load);
	}

	@Override
	public void submit(String pythonScript) {
		//TODO: Connect to add task
	}
}
