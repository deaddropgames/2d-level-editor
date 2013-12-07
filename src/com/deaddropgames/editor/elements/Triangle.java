package com.deaddropgames.editor.elements;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;

import com.deaddropgames.editor.Globals;
import com.deaddropgames.editor.gui.PropertiesPanel;

public class Triangle implements IDrawableElement {
	
	public WorldPoint first, second, third;
	
	public transient Rectangle2D.Float boundingBox;
	private transient boolean selected;
	private transient float zoomFactor;

	public Triangle(final WorldPoint first, final WorldPoint second, final WorldPoint third) {
		
		this.first = new WorldPoint(first);
		this.second = new WorldPoint(second);
		this.third = new WorldPoint(third);
	}

	@Override
	public void init() {
		
		if(zoomFactor != 0.0f) {
			
			initBoundingBox();
		}
	}
	
	private void initBoundingBox() {
		
		float minX = Math.min(Math.min(first.x, second.x), third.x);
		float minY = Math.min(Math.min(first.y, second.y), third.y);
		float maxX = Math.max(Math.max(first.x, second.x), third.x);
		float maxY = Math.max(Math.max(first.y, second.y), third.y);
		boundingBox = new Rectangle2D.Float(minX * zoomFactor, 
				minY * zoomFactor, 
				Math.abs(maxX - minX) * zoomFactor, 
				Math.abs(maxY - minY) * zoomFactor);
	}

	@Override
	public void draw(Graphics gfx, float zoomFactor) {
		
		Color temp = gfx.getColor();
		if(selected) {
			
			gfx.setColor(Globals.SELECTED_COLOR);
		}
		
		gfx.drawLine(Math.round(first.x * zoomFactor), 
				Math.round(first.y * zoomFactor), 
				Math.round(second.x * zoomFactor), 
				Math.round(second.y * zoomFactor));
		
		gfx.drawLine(Math.round(second.x * zoomFactor), 
				Math.round(second.y * zoomFactor), 
				Math.round(third.x * zoomFactor), 
				Math.round(third.y * zoomFactor));
		
		gfx.drawLine(Math.round(third.x * zoomFactor), 
				Math.round(third.y * zoomFactor), 
				Math.round(first.x * zoomFactor), 
				Math.round(first.y * zoomFactor));
		
		gfx.setColor(temp);
		
		// if zoom factor has changed, update our bounding box
		if(this.zoomFactor != zoomFactor) {
		
			this.zoomFactor = zoomFactor;
			initBoundingBox();
		}
		
		// for testing
//		if(boundingBox != null) {
//			
//			gfx.drawRect(Math.round(boundingBox.x), 
//					Math.round(boundingBox.y), 
//					Math.round(boundingBox.width), 
//					Math.round(boundingBox.height));
//		}
	}

	@Override
	public boolean hitTest(int x, int y) {

		if(boundingBox == null) {
			
			return false;
		}
		
		if(boundingBox.contains(x, y)) {
			
			// courtesy of: http://www.blackpawn.com/texts/pointinpoly/
			WorldPoint v0 = third.sub(first);
			WorldPoint v1 = second.sub(first);
			WorldPoint v2 = new WorldPoint(x / zoomFactor, y / zoomFactor).sub(first);
			
			float dot00 = v0.dot(v0);
			float dot01 = v0.dot(v1);
			float dot02 = v0.dot(v2);
			float dot11 = v1.dot(v1);
			float dot12 = v1.dot(v2);
			
			float invDenom = 1 / (dot00 * dot11 - dot01 * dot01);
			float u = (dot11 * dot02 - dot01 * dot12) * invDenom;
			float v = (dot00 * dot12 - dot01 * dot02) * invDenom;
			return (u >= 0) && (v >= 0) && (u + v < 1);
		}
		
		return false;
	}

	@Override
	public PropertiesPanel getPropertiesPanel() {

		return null;
	}

	@Override
	public void setSelected(boolean selected) {
	}

	@Override
	public WorldPoint getSelectedPoint() {
		
		return null;
	}
}
