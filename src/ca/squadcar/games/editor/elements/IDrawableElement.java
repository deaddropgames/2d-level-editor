package ca.squadcar.games.editor.elements;

import java.awt.Graphics;

import ca.squadcar.games.editor.gui.PropertiesPanel;

public interface IDrawableElement {

	public void draw(Graphics gfx, final float zoomFactor);
	
	public boolean hitTest(final float x, final float y);
	
	public PropertiesPanel getPropertiesPanel();
	
	public void setSelected(boolean selected);
}
