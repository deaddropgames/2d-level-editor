package ca.squadcar.games.editor;

import java.awt.Graphics;
import java.awt.geom.Rectangle2D;

public class Line implements IDrawableElement {
	
	public WorldPoint start;
	public WorldPoint end;
	
	private transient Rectangle2D.Float boundingBox;
	
	public Line(final WorldPoint start, final WorldPoint end) {
		
		this.start = new WorldPoint(start);
		this.end = new WorldPoint(end);
		
		initBoundingBox();
	}
	
	public Line(final Line line) {
		
		this.start = new WorldPoint(line.start);
		this.end = new WorldPoint(line.end);
		
		initBoundingBox();
	}
	
	public void setEnd(final WorldPoint end) {

		this.end = new WorldPoint(end);
		
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
	}

	@Override
	public boolean hitTest(float x, float y) {
		
		if(boundingBox == null) {
			
			return false;
		}
		
		// TODO if its in the bounding box, perhaps test how far from the line it is?
		return boundingBox.contains(x, y);
	}

	@Override
	public PropertiesPanel getPropertiesPanel() {
		
		return new LinePanel(this);
	}
}
