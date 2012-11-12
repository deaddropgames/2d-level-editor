package ca.squadcar.games.editor.gui;

import java.awt.Graphics;

import javax.swing.JPanel;

import ca.squadcar.games.editor.JsonElement;
import ca.squadcar.games.editor.JsonLevel;
import ca.squadcar.games.editor.elements.BipedReference;
import ca.squadcar.games.editor.elements.IDrawableElement;
import ca.squadcar.games.editor.elements.Line;
import ca.squadcar.games.editor.elements.QuadraticBezierCurve;
import ca.squadcar.games.editor.elements.WorldPoint;

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
	private IDrawableElement lastHitElement;
	
	/**
	 * Custom panel for drawing onto
	 */
	public LevelCanvas() {
		
		setBackground(Color.WHITE);
		
		elements = new ArrayList<IDrawableElement>();
		zoomFactor = 10.0f;
		temp = null;
		canvasDim = null;
		bipedRef = new BipedReference();
		lastHitElement = null;
		
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
	
	public float getZoomFactor() {
		
		return zoomFactor;
	}
	
	public void zoomIn(final int factor) {
		
		zoomFactor *= factor;
	}
	
	public void zoomOut(final int factor) {
		
		zoomFactor /= factor;
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
	
	public ca.squadcar.games.editor.export.Level getLevelForExport() {
		
		if(elements.size() == 0) {
			
			return null;
		}
	
		ca.squadcar.games.editor.export.Level level = new ca.squadcar.games.editor.export.Level();
		
		// assume a single polyline for now...
		level.polyLines = new ca.squadcar.games.editor.export.PolyLine[1];
		level.polyLines[0] = new ca.squadcar.games.editor.export.PolyLine();
		
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

		lastHitElement = null;
		
		// convert the mouse point to its world point
		for(IDrawableElement element : elements) {
			
			if(element.hitTest(point.x, point.y)) {
				
				lastHitElement = element;
				return true;
			}
		}
		
		return false;
	}
	
	public IDrawableElement getLastHitElement() {
		
		return lastHitElement;
	}
	
	public WorldPoint updateNeighbors(final IDrawableElement element) {
		
		// if we are modifying the last element, then we need to tell the main frame to update its last point variable...
		WorldPoint lastPoint = null;
		
		int index = elements.indexOf(element);
		if(index == -1) {
			
			return lastPoint;
		}
		
		int prev = index - 1;
		int next = index + 1;
		
		// update the previous neighbor...
		if(prev >= 0) {
			
			// get the point that we need to update
			WorldPoint point = getStartPoint(element);
			
			// update the appropriate point on the neighbor
			if(point != null) {
				
				setEndPoint(point, elements.get(prev));
			}
		}
	
		// update the next neighbor...
		if(next < elements.size()) {
			
			// get the point that we need to update
			WorldPoint point = getEndPoint(element);
			
			// update the appropriate point on the neighbor
			if(point != null) {
				
				setStartPoint(point, elements.get(next));
			}
		}
		
		// if we just modified the last element in the list, we need to tell the main frame...
		if(next == elements.size()) {
		
			lastPoint = getLastPoint();
		}
		
		return lastPoint;
	}
	
	public WorldPoint getLastPoint() {
		
		WorldPoint lastPoint = null;
		
		if(elements.size() > 0)  {
			
			lastPoint = getEndPoint(elements.get(elements.size() - 1));
		}
		
		return lastPoint;
	}
	
	public void selectNone() {
	
		for(IDrawableElement element : elements) {
			
			element.setSelected(false);
		}
	}
	
	public WorldPoint deleteElement(IDrawableElement element) {
		
		WorldPoint lastPoint = null;
		
		int index = elements.indexOf(element);
		if(index == -1) {
			
			return lastPoint;
		}
		
		// this should be the start point...
		if(index == 0) {
			
			// if it just the start point, delete it
			// otherwise do nothing, since we need the start point...
			if(elements.size() == 1) {
				
				elements.clear();
			}
		} else {

			boolean isLast = (index == (elements.size() - 1));
			
			// if it was the last element, we just need to tell the main frame to update its current point
			if(isLast) {
				
				elements.remove(index);
				lastPoint = getLastPoint();
			} else { // otherwise we need to update the next element
				
				setStartPoint(getStartPoint(element), elements.get(index + 1));
				elements.remove(index);
			}
		}
		
		return lastPoint;
	}
	
	private static WorldPoint getStartPoint(IDrawableElement element) {
		
		WorldPoint point = null;
		if(element instanceof WorldPoint) {
			
			point = (WorldPoint)element;
		} else if(element instanceof Line) {
			
			point = ((Line)element).start;
		} else if(element instanceof QuadraticBezierCurve) {
			
			point = ((QuadraticBezierCurve)element).first;
		}
		
		return point;
	}
	
	private static WorldPoint getEndPoint(IDrawableElement element) {
		
		WorldPoint point = null;
		if(element instanceof WorldPoint) {
			
			point = (WorldPoint)element;
		} else if(element instanceof Line) {
			
			point = ((Line)element).end;
		} else if(element instanceof QuadraticBezierCurve) {
			
			point = ((QuadraticBezierCurve)element).third;
		}
		
		return point;
	}
	
	private static void setStartPoint(final WorldPoint point, IDrawableElement element) {
		
		if(element instanceof WorldPoint) {
			
			((WorldPoint)element).x = point.x;
			((WorldPoint)element).y = point.y;
		} else if(element instanceof Line) {
			
			((Line)element).start.x = point.x;
			((Line)element).start.y = point.y;
			((Line)element).initBoundingBox();
		} else if(element instanceof QuadraticBezierCurve) {
			
			((QuadraticBezierCurve)element).first.x = point.x;
			((QuadraticBezierCurve)element).first.y = point.y;
			((QuadraticBezierCurve)element).init();
		}
	}
	
	private static void setEndPoint(final WorldPoint point, IDrawableElement element) {
		
		if(element instanceof WorldPoint) {
			
			((WorldPoint)element).x = point.x;
			((WorldPoint)element).y = point.y;
		} else if(element instanceof Line) {
			
			((Line)element).end.x = point.x;
			((Line)element).end.y = point.y;
			((Line)element).initBoundingBox();
		} else if(element instanceof QuadraticBezierCurve) {
			
			((QuadraticBezierCurve)element).third.x = point.x;
			((QuadraticBezierCurve)element).third.y = point.y;
			((QuadraticBezierCurve)element).init();
		}
	}
}
