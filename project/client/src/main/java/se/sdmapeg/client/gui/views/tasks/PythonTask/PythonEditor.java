package se.sdmapeg.client.gui.views.tasks.PythonTask;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sdmapeg.common.tasks.PythonTask;

/**
 * Graphical editor for creating Python scripts.
 */
public class PythonEditor implements ActionListener {
	private static final Logger LOG = LoggerFactory.getLogger(PythonEditor.class);
	private final PythonEditorListener listener;
	private final JFrame frame;
	private final JTextArea textArea;

	public PythonEditor(PythonEditorListener listener, PythonTask task, boolean readonly) {
		this(listener);
		textArea.setText(task.getPythonCode());
		textArea.setEditable(false);
	}

	public PythonEditor(PythonEditorListener listener, File file) {
		this(listener);

		if (file != null) {
			try {
				textArea.setText(readFile(file.getPath(), StandardCharsets.UTF_8));
			} catch (IOException e) {
				LOG.error("Couldn't load file.");
			}
		}
	}

	public PythonEditor(PythonEditorListener listener) {
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
		JButton confirmButton = new JButton("Add task");
		box.add(confirmButton);
		frame.add(box, BorderLayout.SOUTH);
		frame.setLocationByPlatform(true);
		frame.setVisible(true);
		frame.setSize(1024, 800);
		confirmButton.addActionListener(this);
	}

	private String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return encoding.decode(ByteBuffer.wrap(encoded)).toString();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		frame.dispose();
		listener.finishedEditing(textArea.getText());
	}

	public interface PythonEditorListener {
		void finishedEditing(String pythonScript);
	}
}
