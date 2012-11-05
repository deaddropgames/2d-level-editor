package ca.squadcar.games.editor;

import java.awt.Graphics;

public class Line implements IDrawableElement, IBoundingBox {
	
	public WorldPoint start;
	public WorldPoint end;
	
	public Line(final WorldPoint start, final WorldPoint end) {
		
		this.start = new WorldPoint(start);
		this.end = new WorldPoint(end);
	}
	
	public Line(final Line line) {
		
		this.start = new WorldPoint(line.start);
		this.end = new WorldPoint(line.end);
	}
	
	public void setEnd(final WorldPoint end) {

		this.end = new WorldPoint(end);
	}

	@Override
	public boolean containsPoint(float x, float y) {
		
		float minX = Math.min(start.x, end.x);
		float maxX = Math.max(start.x, end.x);
		float minY = Math.min(start.y, end.y);
		float maxY = Math.max(start.y, end.y);
		
		// simpler to test if it doesn't contain the point, rather than if it does
		if(x < minX || x > maxX || y < minY || y > maxY) {
			
			return false;
		}
		
		return true;
	}

	@Override
	public void draw(Graphics gfx, float zoomFactor) {
		
		gfx.drawLine(Math.round(start.x * zoomFactor), 
				Math.round(start.y * zoomFactor), 
				Math.round(end.x * zoomFactor), 
				Math.round(end.y * zoomFactor));
		
		// we only draw the end, since the previous element in the chain will draw this one...
		end.draw(gfx, zoomFactor);
	}

	@Override
	public boolean hitTest(int x, int y, float zoomFactor) {
		
		// TODO Auto-generated method stub
		return false;
	}
}
