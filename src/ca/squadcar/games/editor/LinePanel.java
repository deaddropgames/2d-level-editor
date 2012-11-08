package ca.squadcar.games.editor;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import java.util.ResourceBundle;

import java.awt.GridLayout;

@SuppressWarnings("serial")
public class LinePanel extends JPanel {

	private Line line;
	
	/**
	 * Create the panel.
	 */
	public LinePanel(Line line) {
		
		this.line = line;
		setBorder(new TitledBorder(null, ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LinePanel.title"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new GridLayout(2, 1, 0, 0));
		
		WorldPointPanel startPoint = new WorldPointPanel(line.start,
				ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LinePanel.startTitle"));
		add(startPoint);
		
		WorldPointPanel endPoint = new WorldPointPanel(line.end,
				ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("LinePanel.endTitle"));
		add(endPoint);
	}
}
