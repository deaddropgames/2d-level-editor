package ca.squadcar.games.editor;

import java.awt.Graphics;

import javax.swing.JPanel;

import com.google.gson.Gson;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class LevelCanvas extends JPanel {

	private ArrayList<IDrawableElement> elements;
	private float zoomFactor;
	private IDrawableElement temp;
	private Dimension canvasDim;
	private BipedReference bipedRef;
	
	/**
	 * Custom panel for drawing onto
	 */
	public LevelCanvas() {
		
		setBackground(Color.WHITE);
		
		elements = new ArrayList<IDrawableElement>();
		zoomFactor = 1.0f;
		temp = null;
		canvasDim = null;
		bipedRef = new BipedReference();
		
		reset();
	}

	@Override
	public void paint(Graphics gfx) {
		
		super.paint(gfx);
		
		for(IDrawableElement element : elements) {
			
			element.draw(gfx, zoomFactor);
		}
		
		if(temp != null) {
			
			temp.draw(gfx, zoomFactor);
		}
		
		bipedRef.draw(gfx, zoomFactor);
	}
	
	public void setCursor(int cursor) {
		
		setCursor(Cursor.getPredefinedCursor(cursor));
	}
	
	public void addDrawableElement(final IDrawableElement element) {
		
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
	public void setTempDrawableElement(IDrawableElement temp) {
		
		this.temp = temp;
	}
	
	public void setCanvasDimension(Dimension dim) {
		
		canvasDim = new Dimension(dim);
	}
	
	public boolean hasElements() {
		
		return (elements.size() > 0);
	}
	
	public ca.squadcar.games.export.Level getLevelForExport() {
		
		if(elements.size() == 0) {
			
			return null;
		}
	
		ca.squadcar.games.export.Level level = new ca.squadcar.games.export.Level();
		
		// assume a single polyline for now...
		level.polyLines = new ca.squadcar.games.export.PolyLine[1];
		level.polyLines[0] = new ca.squadcar.games.export.PolyLine();
		
		// we need to translate all points relative to the first
		WorldPoint transPoint;
		IDrawableElement firstElem = elements.get(0);
		if(firstElem instanceof WorldPoint) {
			
			transPoint = new WorldPoint((WorldPoint)firstElem);
		} else { // it's a curve
			
			transPoint = new WorldPoint(((QuadraticBezierCurve)firstElem).first);
		}
		
		WorldPoint currPoint;
		ArrayList<WorldPoint> points = new ArrayList<WorldPoint>();
		points.add(new WorldPoint(0.0f, 0.0f)); // first point is always at the origin
		boolean isFirst = true;
		for(IDrawableElement element : elements) {
			
			if(isFirst) {
				
				isFirst = false;
				continue;
			}
			
			if(element instanceof WorldPoint) {
				
				currPoint = new WorldPoint((WorldPoint)element);
				currPoint.x -= transPoint.x;
				currPoint.y -= transPoint.y;
				currPoint.y *= -1.0f;
				points.add(currPoint);
			} else if(element instanceof Line) {
				
				Line line = (Line)element;
				
				currPoint = new WorldPoint(line.end);
				currPoint.x -= transPoint.x;
				currPoint.y -= transPoint.y;
				currPoint.y *= -1.0f;
				points.add(new WorldPoint(currPoint));
			} else if(element instanceof QuadraticBezierCurve) {
				
				QuadraticBezierCurve curve = (QuadraticBezierCurve)element;
				for(Line line : curve.getLines()) {
					
					currPoint = new WorldPoint(line.end);
					currPoint.x -= transPoint.x;
					currPoint.y -= transPoint.y;
					currPoint.y *= -1.0f;
					points.add(new WorldPoint(currPoint));
				}
			}
		}
		
		if(points.size() <= 1) {
			
			return null;
		}
		
		level.polyLines[0].points = new WorldPoint[points.size()];
		points.toArray(level.polyLines[0].points);
		
		return level;
	}
	
	public JsonLevel getLevelForSave() {
		
		if(elements.size() == 0) {
			
			return null;
		}
		
		IDrawableElement element;
		JsonLevel level = new JsonLevel(elements.size());
		for(int ii = 0; ii < elements.size(); ii++) {
			
			element = elements.get(ii);
			if(element instanceof WorldPoint) {
				
				level.elements[ii] = new JsonElement((WorldPoint)element);
			} else if(element instanceof Line) {
				
				level.elements[ii] = new JsonElement((Line)element);
			} else if(element instanceof QuadraticBezierCurve) {

				level.elements[ii] = new JsonElement((QuadraticBezierCurve)element);
			}
		}

		return level;
	}
	
	public void reset() {
		
		elements.clear();
	}
	
	public boolean loadLevelFromFile(final File levelFile) throws IOException {
		
		BufferedReader br = null;
		JsonLevel jsonLevel = null;
		try {
			
			br = new BufferedReader(new FileReader(levelFile));
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while (line != null) {
				
	            sb.append(line);
	            sb.append("\n");
	            line = br.readLine();
	        }
			
			Gson gson = new Gson();
			jsonLevel = gson.fromJson(sb.toString(), JsonLevel.class);
		} catch(Exception ex) {
			
			ex.printStackTrace();
			return false;
		} finally {
			
			if(br != null) {
				
				br.close();
			}
		}
		
		if(jsonLevel == null) {
			
			return false;
		}
		
		for(JsonElement jsonElement : jsonLevel.elements) {
			
			IDrawableElement element = jsonElement.toDrawableElement();
			if(element == null) {
				
				return false;
			}
			
			elements.add(element);
		}
		
		return true;
	}
	
	public Dimension getCanvasDimension() {
		
		return canvasDim;
	}
	
	public void updateForViewportChange(final Point point) {
		
		bipedRef.setOffset(point);
	}
	
	public boolean hitTest(final WorldPoint point) {
		
		boolean retVal = false;
		for(IDrawableElement element : elements) {
			
			if(element.hitTest(point.x, point.y, zoomFactor)) {
				
				retVal = true;
				break;
			}
		}
		
		return retVal;
	}
}
