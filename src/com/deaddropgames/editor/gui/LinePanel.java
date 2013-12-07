package com.deaddropgames.editor.gui;

import javax.swing.border.TitledBorder;

import com.deaddropgames.editor.elements.Line;
import com.deaddropgames.editor.events.ElementChangedEvent;
import com.deaddropgames.editor.events.IElementChangedListener;

import java.util.ResourceBundle;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

@SuppressWarnings("serial")
public class LinePanel extends PropertiesPanel implements IElementChangedListener {
	
	/**
	 * Create the panel.
	 */
	public LinePanel(final Line line) {
		
		super(line);
		
		setBorder(new TitledBorder(null, ResourceBundle.getBundle("com.deaddropgames.editor.messages").getString("LinePanel.title"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		WorldPointPanel startPointPanel = new WorldPointPanel(line.start,
				ResourceBundle.getBundle("com.deaddropgames.editor.messages").getString("LinePanel.startTitle"));
		startPointPanel.addElemChangedListener(this);
		GridBagConstraints gbc_startPoint = new GridBagConstraints();
		gbc_startPoint.fill = GridBagConstraints.BOTH;
		gbc_startPoint.insets = new Insets(0, 0, 5, 0);
		gbc_startPoint.gridx = 0;
		gbc_startPoint.gridy = 0;
		add(startPointPanel, gbc_startPoint);
		
		WorldPointPanel endPointPanel = new WorldPointPanel(line.end,
				ResourceBundle.getBundle("com.deaddropgames.editor.messages").getString("LinePanel.endTitle"));
		endPointPanel.addElemChangedListener(this);
		GridBagConstraints gbc_endPoint = new GridBagConstraints();
		gbc_endPoint.fill = GridBagConstraints.BOTH;
		gbc_endPoint.gridx = 0;
		gbc_endPoint.gridy = 1;
		add(endPointPanel, gbc_endPoint);
	}

	@Override
	public void elementChanged(ElementChangedEvent event) {
		
		// both points should be updated automatically...just re-init the bounding box
		((Line)element).init();
		
		fireElemChangedEvent();
	}
}
