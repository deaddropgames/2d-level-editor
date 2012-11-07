package ca.squadcar.games.editor;

import java.awt.Graphics;

public class WorldPoint implements IDrawableElement {
	
	public float x;
	public float y;
	
	public WorldPoint(final float x, final float y) {
		
		this.x = x;
		this.y = y;
	}
	
	public WorldPoint(final WorldPoint point) {
		
		this.x = point.x;
		this.y = point.y;
	}
	
	public WorldPoint mul(final float scalar) {
		
		return new WorldPoint(this.x * scalar, this.y * scalar);
	}

	@Override
	public void draw(Graphics gfx, float zoomFactor) {
		
		// draw a circle
		gfx.drawOval(Math.round(x * zoomFactor) - Globals.POINT_SIZE / 2, 
				Math.round(y * zoomFactor) - Globals.POINT_SIZE / 2, 
				Globals.POINT_SIZE, 
				Globals.POINT_SIZE);
	}

	@Override
	public boolean hitTest(float x, float y) {
		
		// simpler to test if it doesn't contain the point, rather than if it does
		/*if(Math.abs(x - this.x) > Globals.POINT_SIZE || Math.abs(y - this.y) > Globals.POINT_SIZE) {
			
			return false;
		}*/
		
		return false;
	}
}
