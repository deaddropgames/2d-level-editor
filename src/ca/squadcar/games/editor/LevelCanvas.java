package ca.squadcar.games.editor;

import java.awt.Graphics;

import javax.swing.JPanel;

import org.ini4j.Ini;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class LevelCanvas extends JPanel {
	
	private ArrayList<DrawableElement> elements;
	private float zoomFactor;
	private DrawableElement temp;
	private Dimension canvasDim;
	
	/**
	 * Custom panel for drawing onto
	 */
	public LevelCanvas() {
		
		setBackground(Color.WHITE);
		
		elements = new ArrayList<DrawableElement>();
		zoomFactor = 1.0f;
		temp = null;
		canvasDim = null;
	}

	@Override
	public void paint(Graphics gfx) {
		
		super.paint(gfx);
		
		for(DrawableElement element : elements) {
			
			element.draw(gfx, zoomFactor);
		}
		
		if(temp != null) {
			
			temp.draw(gfx, zoomFactor);
		}
	}
	
	public void setCursor(int cursor) {
		
		setCursor(Cursor.getPredefinedCursor(cursor));
	}
	
	public void addDrawableElement(final DrawableElement element) {
		
		elements.add(element);
	}
	
	public void setZoomFactor(final float zoomFactor) {
		
		this.zoomFactor = zoomFactor;
		
		if(canvasDim != null) {
			
			Dimension dim = new Dimension(canvasDim);
			dim.height *= zoomFactor;
			dim.width *= zoomFactor;
			setPreferredSize(dim);
		}
	}
	
	/**
	 * The temp element is used to draw an element that hasn't been finalized; e.g.: the current poly line that is being drawn.
	 */
	public void setTempDrawableElement(DrawableElement temp) {
		
		this.temp = temp;
	}
	
	public void setCanvasDimension(Dimension dim) {
		
		canvasDim = new Dimension(dim);
	}
	
	public boolean hasElements() {
		
		return (elements.size() > 0);
	}
	
	public void saveToFile(Ini ini, LevelEditorSettings settings) {
		
		for(DrawableElement element : elements) {
			
			element.saveToFile(ini);
		}
	}
	
	public void reset() {
		
		elements.clear();
	}
	
	public boolean loadLevelFromFile(final File levelFile) {
		
		boolean retVal = true;
		try {
			
			Ini ini = new Ini(levelFile);
		    if(ini.isEmpty()) {
				
		    	System.err.println(String.format("Input INI file '%s' is empty.", levelFile.getName()));
				return false;
			}
		    
		    // clear the canvas
		    reset();
		    
		    // get our supported sections - currently this is just poly lines
		    Ini.Section levelSection = ini.get("level");
		    String[] polyLineNames = levelSection.getAll("polyline", String[].class);
		    PolyLine currPolyLine = null;
		    for(String polyLineName : polyLineNames) {
		    	
		    	// get x an y points for this line string
		    	Ini.Section currLineStringSection = ini.get(polyLineName);
		    	float[] xVals = currLineStringSection.getAll("x", float[].class);
		    	float[] yVals = currLineStringSection.getAll("y", float[].class);
		    	
		    	// sanity check
		    	if(xVals.length != yVals.length) {
		    		
		    		System.err.println(String.format("Section '%s' has uneven number of x and y values.", polyLineName));
		    		retVal = false;
		    		continue;
		    	}
		    	
		    	currPolyLine = new PolyLine();
		    	for(int ii = 0; ii < xVals.length; ii++) {
			    	
		    		// don't forget to flip the points y-vals!!
		    		currPolyLine.addPoint(new WorldPoint(xVals[ii], -yVals[ii]));
		    	}
		    	
		    	elements.add(new PolyLine(currPolyLine));
		    	currPolyLine = null;
		    }
		    
		} catch(Exception ex) {
			
			ex.printStackTrace();
			return false;
		}
		
		return retVal;
	}
	
	public Dimension getCanvasDimension() {
		
		return canvasDim;
	}
}
