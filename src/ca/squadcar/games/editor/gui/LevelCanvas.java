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
			WorldPoint point = null;
			if(element instanceof WorldPoint) {
				
				point = (WorldPoint)element;
			} else if(element instanceof Line) {
				
				point = ((Line)element).start;
			} else if(element instanceof QuadraticBezierCurve) {
				
				point = ((QuadraticBezierCurve)element).first;
			}
			
			// update the appropriate point on the neighbor
			IDrawableElement prevElement = elements.get(prev);
			if(point != null) {
				
				if(prevElement instanceof WorldPoint) {
					
					((WorldPoint)prevElement).x = point.x;
					((WorldPoint)prevElement).y = point.y;
				} else if(prevElement instanceof Line) {
					
					((Line)prevElement).end.x = point.x;
					((Line)prevElement).end.y = point.y;
					((Line)prevElement).initBoundingBox();
				} else if(prevElement instanceof QuadraticBezierCurve) {
					
					((QuadraticBezierCurve)prevElement).third.x = point.x;
					((QuadraticBezierCurve)prevElement).third.y = point.y;
					((QuadraticBezierCurve)prevElement).init();
				}
			}
		}
	
		// update the next neighbor...
		if(next < elements.size()) {
			
			// get the point that we need to update
			WorldPoint point = null;
			if(element instanceof WorldPoint) {
				
				point = (WorldPoint)element;
			} else if(element instanceof Line) {
				
				point = ((Line)element).end;
			} else if(element instanceof QuadraticBezierCurve) {
				
				point = ((QuadraticBezierCurve)element).third;
			}
			
			// update the appropriate point on the neighbor
			IDrawableElement nextElement = elements.get(next);
			if(point != null) {
				
				if(nextElement instanceof WorldPoint) {
					
					((WorldPoint)nextElement).x = point.x;
					((WorldPoint)nextElement).y = point.y;
				} else if(nextElement instanceof Line) {
					
					((Line)nextElement).start.x = point.x;
					((Line)nextElement).start.y = point.y;
					((Line)nextElement).initBoundingBox();
				} else if(nextElement instanceof QuadraticBezierCurve) {
					
					((QuadraticBezierCurve)nextElement).first.x = point.x;
					((QuadraticBezierCurve)nextElement).first.y = point.y;
					((QuadraticBezierCurve)nextElement).init();
				}
			}
		}
		
		// if we just modified the last element in the list, we need to tell the main frame...
		if(next == elements.size()) {
		
			if(element instanceof WorldPoint) {
				
				lastPoint = (WorldPoint)element;
			} else if(element instanceof Line) {
				
				lastPoint = ((Line)element).end;
			} else if(element instanceof QuadraticBezierCurve) {
				
				lastPoint = ((QuadraticBezierCurve)element).third;
			}
		}
		
		return lastPoint;
	}
	
	public WorldPoint getLastPoint() {
		
		WorldPoint lastPoint = null;
		
		if(elements.size() > 0)  {
			
			IDrawableElement element = elements.get(elements.size() - 1);
			if(element instanceof WorldPoint) {
				
				lastPoint = (WorldPoint)element;
			} else if(element instanceof Line) {
				
				lastPoint = ((Line)element).end;
			} else if(element instanceof QuadraticBezierCurve) {
				
				lastPoint = ((QuadraticBezierCurve)element).third;
			}
		}
		
		return lastPoint;
	}
	
	public void selectNone() {
	
		for(IDrawableElement element : elements) {
			
			element.setSelected(false);
		}
	}
}
