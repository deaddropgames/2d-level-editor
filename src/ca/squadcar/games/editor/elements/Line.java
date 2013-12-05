package ca.squadcar.games.editor.elements;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;

import ca.squadcar.games.editor.Globals;
import ca.squadcar.games.editor.gui.LinePanel;
import ca.squadcar.games.editor.gui.PropertiesPanel;

public class Line implements IDrawableElement {
	
	public WorldPoint start;
	public WorldPoint end;
	
	private transient Rectangle2D.Float boundingBox;
	private transient boolean selected;
	private transient float zoomFactor;
	private transient WorldPoint selectedPoint;
	
	public Line(final WorldPoint start, final WorldPoint end) {
		
	    this.start = new WorldPoint(start);
		this.end = new WorldPoint(end);

		this.zoomFactor = 0.0f;
		this.selected = false;
		this.selectedPoint = null;
		
		init();
	}
	
	@Override
	public void init() {

		boundingBox = new Rectangle2D.Float(Math.min(start.x, end.x) * zoomFactor, 
				Math.min(start.y, end.y) * zoomFactor, 
				Math.abs(start.x - end.x) * zoomFactor, 
				Math.abs(start.y - end.y) * zoomFactor);
		
		start.init();
		end.init();
		
		if(start.boundingBox != null) {
			
			boundingBox = (Float)boundingBox.createUnion(start.boundingBox);
		}
		
		if(end.boundingBox != null) {
		
			boundingBox = (Float)boundingBox.createUnion(end.boundingBox);
		}
	}
	
	// sometimes the parent won't want to draw the end point circles...
	public void childDraw(Graphics gfx, float zoomFactor) {
		
		gfx.drawLine(Math.round(start.x * zoomFactor), 
				Math.round(start.y * zoomFactor), 
				Math.round(end.x * zoomFactor), 
				Math.round(end.y * zoomFactor));
		
		if(this.zoomFactor != zoomFactor) {
			
			this.zoomFactor = zoomFactor;
			init();
		}
	}

	@Override
	public void draw(Graphics gfx, final float zoomFactor) {
				
		Color temp = gfx.getColor();
		if(selected) {
			
			gfx.setColor(Globals.SELECTED_COLOR);
		}
		
		start.draw(gfx, zoomFactor);
		
		gfx.drawLine(Math.round(start.x * zoomFactor), 
				Math.round(start.y * zoomFactor), 
				Math.round(end.x * zoomFactor), 
				Math.round(end.y * zoomFactor));
		
		end.draw(gfx, zoomFactor);
		
		// for testing
//		if(boundingBox != null) {
//			
//			gfx.drawRect(Math.round(boundingBox.x), 
//					Math.round(boundingBox.y), 
//					Math.round(boundingBox.width), 
//					Math.round(boundingBox.height));
//		}
		
		gfx.setColor(temp);
		
		// if zoom factor has changed, update our bounding box
		if(this.zoomFactor != zoomFactor) {
		
			this.zoomFactor = zoomFactor;
			init();
		}
	}

	@Override
	public boolean hitTest(final int x, final int y) {
		
		if(boundingBox == null) {
			
			return false;
		}
		
		this.selectedPoint = null;
		
		// if either point is hit...
		if(start.hitTest(x, y) || end.hitTest(x, y)) {
			
			this.selectedPoint = start.hitTest(x, y) ? start : end;
			return true;
		}
		
		// if the bounding box contains the point, calculate how close the point is to the line
		float distance = 1000.0f;
		if(boundingBox.contains(x, y)) {
			
			float fX = x / zoomFactor;
			float fY = y / zoomFactor;
			
			// edge cases...
			// vertical line
			if(Math.abs(start.x - end.x) < 0.001) {
				
				distance = Math.abs(start.x - fX);
			} else if(Math.abs(start.y - end.y) < 0.001) { // horizontal line
				
				distance = Math.abs(start.y - fY);
			} else { // do some math to figure it out...
				
				// get basic properties of the line
				float slope = (end.y - start.y) / (end.x - start.x);
				float intercept = end.y - slope * end.x;
				float slopeSquared = slope * slope;
				
				// figure out where the lines cross
				float y2 = (fY * slopeSquared + fX * slope + intercept) / (slopeSquared + 1);
				float x2 = (y2 - intercept) / slope;
				
				// compute the distance between the points
				float deltaX = x2 - fX;
				float deltaY = y2 - fY;
				distance = (float)Math.sqrt(deltaX * deltaX + deltaY * deltaY);
			}
		}
		
		return (distance < Globals.HIT_TEST_DIST);
	}

	@Override
	public PropertiesPanel getPropertiesPanel() {
		
		return new LinePanel(this);
	}

	@Override
	public void setSelected(boolean selected) {

		this.selected = selected;
	}

	@Override
	public WorldPoint getSelectedPoint() {
		
		return this.selectedPoint;
	}
}
