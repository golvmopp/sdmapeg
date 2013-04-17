package se.sdmapeg.client.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

/**
 * Graphical editor for creating Python scripts.
 */
public class PythonEditor implements ActionListener {
	private final ActionListener listener;
	private final JFrame frame;
	private final JTextArea textArea;

	private PythonEditor(ActionListener listener) {
		this.listener = listener;

		frame = new JFrame("Python editor");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		Box mainContainer = new Box(BoxLayout.PAGE_AXIS);
		frame.add(mainContainer);
		textArea = new RSyntaxTextArea(
				new RSyntaxDocument(RSyntaxDocument.SYNTAX_STYLE_PYTHON));
		textArea.setFont(Font.decode("Monospaced"));
		frame.add(new JScrollPane(textArea), BorderLayout.CENTER);
		Box box = new Box(BoxLayout.LINE_AXIS);
		JButton confirmButton = new JButton("Send task");
		box.add(confirmButton);
		frame.add(box, BorderLayout.SOUTH);
		frame.setLocationByPlatform(true);
		frame.setVisible(true);
		frame.setSize(1024, 800);
		confirmButton.addActionListener(this);
	}

	public static PythonEditor newPythonEditor(ActionListener listener) {
		return new PythonEditor(listener);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ActionEvent event = new ActionEvent(textArea, 0, "pythonCode");
		listener.actionPerformed(event);
		frame.dispose();
	}
}
