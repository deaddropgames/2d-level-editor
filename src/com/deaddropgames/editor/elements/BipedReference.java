package com.deaddropgames.editor.elements;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import com.deaddropgames.editor.gui.PropertiesPanel;

public class BipedReference implements IDrawableElement {

	private float height;
	private float width;
	
	// origin...
	private float offsetX;
	private float offsetY;
	
	public BipedReference() {
		
		height = 1.8f;
		width = 0.3f;
		
		offsetX = 0.0f;
		offsetY = 0.0f;
	}
	
	@Override
	public void draw(Graphics gfx, float zoomFactor) {
		
		// change the color for the reference
		Color temp = gfx.getColor();
		gfx.setColor(Color.GRAY);
		
		// biped head
		gfx.drawOval(Math.round((offsetX + 0.65f) * zoomFactor), 
				Math.round(offsetY * zoomFactor), 
				Math.round(0.30f * zoomFactor), 
				Math.round(0.30f * zoomFactor));
		
		// draw biped body
		gfx.drawRect(Math.round((offsetX + 0.65f) * zoomFactor), 
				Math.round((offsetY + 0.3f) * zoomFactor), 
				Math.round(width * zoomFactor), 
				Math.round((height - 0.3f) * zoomFactor));
		
		// draw biped skis
		gfx.drawRect(Math.round(offsetX * zoomFactor), 
				Math.round((offsetY + height) * zoomFactor), 
				Math.round(1.6f * zoomFactor), 
				Math.round(0.1f * zoomFactor));
		
		gfx.setColor(temp);
	}
	
	public void setOffset(final Point point) {
		
		offsetX = point.x;
		offsetY = point.y;
	}

	@Override
	public boolean hitTest(final int x, final int y) {
		
		// no-op, doesn't matter
		return false;
	}

	@Override
	public PropertiesPanel getPropertiesPanel() {
		
		// no-op, doesn't apply
		return null;
	}

	@Override
	public void setSelected(boolean selected) {
		
		// no-op, can't be selected
	}

	@Override
	public WorldPoint getSelectedPoint() {
		
		return null;
	}

	@Override
	public void init() {
	}
}
