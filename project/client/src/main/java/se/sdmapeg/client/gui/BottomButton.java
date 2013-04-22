package se.sdmapeg.client.gui;

import javax.swing.*;
import java.awt.*;

public class BottomButton extends JButton {
	private final Color BACKGROUND_COLOR = Color.WHITE;
	private final Color BORDER_COLOR = Color.BLACK;

	public BottomButton() {
		this("");
	}

	public BottomButton(String text) {
		this(text, false);
	}

	public BottomButton(String text, boolean first) {
		super(text);
		setBackground(BACKGROUND_COLOR);
		setOpaque(true);
		setHorizontalAlignment(CENTER);
		setBorder(BorderFactory.createMatteBorder(1, first ? 1 : 0, 1, 1, BORDER_COLOR));
	}
}
