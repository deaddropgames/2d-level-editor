package ca.squadcar.games.editor;

import java.awt.Graphics;

public interface IDrawableElement {

	public void draw(Graphics gfx, float zoomFactor);
	
	public boolean hitTest(final int x, final int y, final float zoomFactor);
}
