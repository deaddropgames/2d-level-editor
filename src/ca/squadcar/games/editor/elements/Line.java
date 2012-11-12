package ca.squadcar.games.editor.elements;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;

import ca.squadcar.games.editor.Globals;
import ca.squadcar.games.editor.gui.LinePanel;
import ca.squadcar.games.editor.gui.PropertiesPanel;

public class Line implements IDrawableElement {
	
	public WorldPoint start;
	public WorldPoint end;
	
	private transient Rectangle2D.Float boundingBox;
	private transient boolean selected;
	
	public Line(final WorldPoint start, final WorldPoint end) {
		
	    this.start = new WorldPoint(start);
		this.end = new WorldPoint(end);
		
		this.selected = false;
		
		initBoundingBox();
	}
	
	public void initBoundingBox() {
		
		// TODO: when a line is vertical or horizontal, we should expand the bounding box a bit
		this.boundingBox = new Rectangle2D.Float(Math.min(start.x, end.x), 
				Math.min(start.y, end.y), 
				Math.abs(start.x - end.x), 
				Math.abs(start.y - end.y));
	}

	@Override
	public void draw(Graphics gfx, final float zoomFactor) {
		
		Color temp = gfx.getColor();
		if(selected) {
			
			gfx.setColor(Globals.SELECTED_COLOR);
		}
		
		gfx.drawLine(Math.round(start.x * zoomFactor), 
				Math.round(start.y * zoomFactor), 
				Math.round(end.x * zoomFactor), 
				Math.round(end.y * zoomFactor));
		
		// we only draw the end, since the previous element in the chain will draw this one...
		end.draw(gfx, zoomFactor);
		
		/* for testing
		if(boundingBox != null) {
			
			gfx.drawRect(Math.round(boundingBox.x * zoomFactor), 
					Math.round(boundingBox.y * zoomFactor), 
					Math.round(boundingBox.width * zoomFactor), 
					Math.round(boundingBox.height * zoomFactor));
		}
		*/
		
		gfx.setColor(temp);
	}

	@Override
	public boolean hitTest(float x, float y) {
		
		if(boundingBox == null) {
			
			return false;
		}
		
		// if either point is hit...
		if(start.hitTest(x, y) || end.hitTest(x, y)) {
			
			return true;
		}
		
		// TODO if its in the bounding box, perhaps test how far from the line it is?
		return boundingBox.contains(x, y);
	}

	@Override
	public PropertiesPanel getPropertiesPanel() {
		
		return new LinePanel(this);
	}

	@Override
	public void setSelected(boolean selected) {

		this.selected = selected;
	}
}
