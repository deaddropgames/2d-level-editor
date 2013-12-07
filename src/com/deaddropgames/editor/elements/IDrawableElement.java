package com.deaddropgames.editor.elements;

import java.awt.Graphics;

import com.deaddropgames.editor.gui.PropertiesPanel;

public interface IDrawableElement {

	public void draw(Graphics gfx, final float zoomFactor);
	
	public boolean hitTest(final int x, final int y);
	
	public PropertiesPanel getPropertiesPanel();
	
	public void setSelected(boolean selected);
	
	public WorldPoint getSelectedPoint();
	
	public void init();
}
