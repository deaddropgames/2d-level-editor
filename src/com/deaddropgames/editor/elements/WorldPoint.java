package com.deaddropgames.editor.elements;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.util.ResourceBundle;

import com.deaddropgames.editor.Globals;
import com.deaddropgames.editor.gui.PropertiesPanel;
import com.deaddropgames.editor.gui.WorldPointPanel;

public class WorldPoint implements IDrawableElement {
	
	public float x;
	public float y;
	
	private transient float zoomFactor;
	public transient Rectangle2D.Float boundingBox;
	private transient boolean selected;
	
	public WorldPoint(final float x, final float y) {
		
		this.x = x;
		this.y = y;
		this.zoomFactor = 0.0f;
		this.selected = false;
	}
	
	public WorldPoint(final WorldPoint point) {
		
		this.x = point.x;
		this.y = point.y;
		this.zoomFactor = 0.0f;
		this.selected = false;
	}
	
	public WorldPoint mul(final float scalar) {
		
		return new WorldPoint(this.x * scalar, this.y * scalar);
	}
	
	@Override
	public void init() {
		
		if(zoomFactor == 0.0f) {
			
			return;
		}
		
		float size = Globals.POINT_SIZE / zoomFactor;
		float halfSize = size / 2.0f;
		this.boundingBox = new Rectangle2D.Float((x - halfSize) * zoomFactor, 
				(y - halfSize) * zoomFactor, 
				size * zoomFactor, 
				size * zoomFactor);
	}

	@Override
	public void draw(Graphics gfx, float zoomFactor) {

		Color temp = gfx.getColor();
		if(selected) {
			
			gfx.setColor(Globals.SELECTED_COLOR);
		}
		
		// draw a circle
		gfx.drawOval(Math.round(x * zoomFactor) - Globals.POINT_SIZE / 2, 
				Math.round(y * zoomFactor) - Globals.POINT_SIZE / 2, 
				Globals.POINT_SIZE, 
				Globals.POINT_SIZE);
		
		gfx.setColor(temp);
		
		// if zoom factor has changed, update our bounding box
		if(this.zoomFactor != zoomFactor) {
		
			this.zoomFactor = zoomFactor;
			init();
		}
		
		// to debug bounding boxes...
//		if(boundingBox != null) {
//			
//			gfx.drawRect(Math.round(boundingBox.x), 
//					Math.round(boundingBox.y), 
//					Math.round(boundingBox.width), 
//					Math.round(boundingBox.height));
//		}
	}

	@Override
	public boolean hitTest(final int x, final int y) {
		
		if(boundingBox == null) {
			
			return false;
		}
		
		return boundingBox.contains(x, y);
	}

	@Override
	public PropertiesPanel getPropertiesPanel() {
		
		return new WorldPointPanel(this, 
				ResourceBundle.getBundle("com.deaddropgames.editor.messages").getString("WorldPointPanel.title"));
	}

	@Override
	public void setSelected(boolean selected) {
		
		this.selected = selected;
	}

	@Override
	public WorldPoint getSelectedPoint() {
		
		return this.selected ? this : null;
	}
	
	public WorldPoint sub(final WorldPoint other) {
		
		return new WorldPoint(x - other.x, y - other.y);
	}
	
	public float dot(final WorldPoint other) {
		
		return x * other.x + y * other.y;
	}
}
