package ca.squadcar.games.editor;

import java.awt.Graphics;

import org.ini4j.Ini;

public abstract class DrawableElement {

	public abstract void draw(Graphics gfx, float zoomFactor);
	
	public abstract void saveToFile(Ini ini);
	
	public abstract boolean hitTest(final WorldPoint point, float zoomFactor);
}
