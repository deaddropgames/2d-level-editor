package ca.squadcar.games.editor;

import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.util.ResourceBundle;

public class WorldPoint implements IDrawableElement {
	
	public float x;
	public float y;
	
	public transient float zoomFactor;
	public transient Rectangle2D.Float boundingBox;
	
	public WorldPoint(final float x, final float y) {
		
		this.x = x;
		this.y = y;
		this.zoomFactor = 0.0f;
	}
	
	public WorldPoint(final WorldPoint point) {
		
		this.x = point.x;
		this.y = point.y;
		this.zoomFactor = 0.0f;
	}
	
	public WorldPoint mul(final float scalar) {
		
		return new WorldPoint(this.x * scalar, this.y * scalar);
	}
	
	private void initBoundingBox() {
		
		if(zoomFactor == 0.0f) {
			
			return;
		}
		
		float size = Globals.POINT_SIZE / zoomFactor;
		float halfSize = size / 2.0f;
		this.boundingBox = new Rectangle2D.Float(x - halfSize, 
				y - halfSize, 
				size, 
				size);
	}

	@Override
	public void draw(Graphics gfx, float zoomFactor) {
		
		// if zoom factor has changed, update our bounding box
		if(this.zoomFactor != zoomFactor) {
		
			this.zoomFactor = zoomFactor;
			initBoundingBox();
		}
		
		// draw a circle
		gfx.drawOval(Math.round(x * zoomFactor) - Globals.POINT_SIZE / 2, 
				Math.round(y * zoomFactor) - Globals.POINT_SIZE / 2, 
				Globals.POINT_SIZE, 
				Globals.POINT_SIZE);
	}

	@Override
	public boolean hitTest(float x, float y) {
		
		if(boundingBox == null) {
			
			return false;
		}
		
		return boundingBox.contains(x, y);
	}

	@Override
	public PropertiesPanel getPropertiesPanel() {
		
		return new WorldPointPanel(this, 
				ResourceBundle.getBundle("ca.squadcar.games.editor.messages").getString("WorldPointPanel.title"));
	}
}
