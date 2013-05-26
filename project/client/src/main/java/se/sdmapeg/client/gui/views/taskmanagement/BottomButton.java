package se.sdmapeg.client.gui.views.taskmanagement;

import javax.swing.*;
import java.awt.*;

public class BottomButton extends JButton {
	private final Color BACKGROUND_COLOR = Color.WHITE;
	private final Color BORDER_COLOR = Color.BLACK;

	public BottomButton(String text) {
		this(text, false);
	}

	public BottomButton(String text, boolean first) {
		super(text);
		setPreferredSize(new Dimension(0, 25));
		setFont(new Font(null, 0, 16));
		setBackground(BACKGROUND_COLOR);
		setOpaque(true);
		setHorizontalAlignment(CENTER);
		setBorder(BorderFactory.createMatteBorder(1, first ? 0 : 1, 0, 0, BORDER_COLOR));
	}
}
