package se.sdmapeg.client.gui.tasks.PythonTask;

import se.sdmapeg.client.gui.TaskCreator.TaskCreationView;
import se.sdmapeg.common.tasks.PythonTask;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PythonTaskView extends JPanel implements PythonEditor.PythonEditorListener {
	TaskCreationView.TaskCreationListener listener;

	public PythonTaskView(TaskCreationView.TaskCreationListener listener) {
		this.listener = listener;

		setLayout(new BorderLayout());

		JPanel buttons = new JPanel(new GridLayout(2, 1, 10, 10));
		add(buttons, BorderLayout.NORTH);

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
	public void finnishedEditing(String pythonScript) {
		listener.taskFinnished(PythonTask.newPythonTask(pythonScript));
	}
}
