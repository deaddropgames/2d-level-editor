package ca.squadcar.games.editor;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import java.awt.GridLayout;
import java.util.ResourceBundle;

public class CurvePanel extends JPanel {

	private QuadraticBezierCurve curve;
	
	/**
	 * Create the panel.
	 */
	public CurvePanel(QuadraticBezierCurve curve) {
		
		this.curve = curve;
		
		setBorder(new TitledBorder(null, ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("CurvePanel.title"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new GridLayout(3, 1, 0, 0));
		
		WorldPointPanel firstPoint = new WorldPointPanel(curve.first,
				ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("CurvePanel.firstTitle"));
		add(firstPoint);
		
		WorldPointPanel secondPoint = new WorldPointPanel(curve.second,
				ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("CurvePanel.secondTitle"));
		add(secondPoint);
		
		WorldPointPanel thirdPoint = new WorldPointPanel(curve.third,
				ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("CurvePanel.thirdTitle"));
		add(thirdPoint);
	}
}
