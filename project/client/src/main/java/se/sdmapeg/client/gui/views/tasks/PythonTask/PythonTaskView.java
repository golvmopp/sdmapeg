package se.sdmapeg.client.gui.views.tasks.PythonTask;

import se.sdmapeg.client.gui.listeners.TaskCreationListener;
import se.sdmapeg.common.tasks.PythonTask;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PythonTaskView extends JPanel implements PythonEditor.PythonEditorListener {
	TaskCreationListener listener;

	JTextField name;

	public PythonTaskView(TaskCreationListener listener) {
		this.listener = listener;

		setLayout(new BorderLayout());

		JPanel buttons = new JPanel(new GridLayout(4, 1, 10, 10));
		add(buttons, BorderLayout.NORTH);

		JLabel nameLabel = new JLabel("Name:");
		buttons.add(nameLabel);

		name = new JTextField();
		buttons.add(name);

		JButton write = new JButton("Create Python script");
		write.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new PythonEditor(PythonTaskView.this);
			}
		});
		buttons.add(write);

		JButton load = new JButton("Load Python script");
		load.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.showDialog(PythonTaskView.this, "Öppna");
				new PythonEditor(PythonTaskView.this, fileChooser.getSelectedFile());
			}
		});
		buttons.add(load);
	}

	@Override
	public void finishedEditing(String pythonScript) {
		listener.taskFinished(PythonTask.newPythonTask(pythonScript, name.getText()));
	}
}
