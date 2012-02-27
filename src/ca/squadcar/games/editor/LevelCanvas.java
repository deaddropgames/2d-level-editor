package ca.squadcar.games.editor;

import java.awt.Graphics;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class LevelCanvas extends JPanel {
	
	private ArrayList<DrawableElement> elements;
	private float zoomFactor;
	private DrawableElement temp;
	private Dimension canvasDim;
	
	/**
	 * Custom panel for drawing onto
	 */
	public LevelCanvas() {
		
		setBackground(Color.WHITE);
		
		elements = new ArrayList<DrawableElement>();
		zoomFactor = 1.0f;
		temp = null;
		canvasDim = null;
	}

	@Override
	public void paint(Graphics gfx) {
		
		super.paint(gfx);
		
		for(DrawableElement element : elements) {
			
			element.draw(gfx, zoomFactor);
		}
		
		if(temp != null) {
			
			temp.draw(gfx, zoomFactor);
		}
	}
	
	public void setCursor(int cursor) {
		
		setCursor(Cursor.getPredefinedCursor(cursor));
	}
	
	public void addDrawableElement(final DrawableElement element) {
		
		elements.add(element);
	}
	
	public void setZoomFactor(final float zoomFactor) {
		
		this.zoomFactor = zoomFactor;
		
		if(canvasDim != null) {
			
			Dimension dim = new Dimension(canvasDim);
			dim.height *= zoomFactor;
			dim.width *= zoomFactor;
			setPreferredSize(dim);
		}
	}
	
	/**
	 * The temp element is used to draw an element that hasn't been finalized; e.g.: the current poly line that is being drawn.
	 */
	public void setTempDrawableElement(DrawableElement temp) {
		
		this.temp = temp;
	}
	
	public void setCanvasDimension(Dimension dim) {
		
		canvasDim = new Dimension(dim);
	}
}
