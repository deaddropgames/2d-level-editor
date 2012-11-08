package ca.squadcar.games.editor;

import java.awt.Graphics;

import javax.swing.JPanel;

public interface IDrawableElement {

	public void draw(Graphics gfx, final float zoomFactor);
	
	public boolean hitTest(final float x, final float y);
	
	public JPanel getPropertiesPanel();
}
