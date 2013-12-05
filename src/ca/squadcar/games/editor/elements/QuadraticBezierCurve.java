package ca.squadcar.games.editor.elements;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.util.ArrayList;

import ca.squadcar.games.editor.Globals;
import ca.squadcar.games.editor.gui.CurvePanel;
import ca.squadcar.games.editor.gui.PropertiesPanel;

public class QuadraticBezierCurve implements IDrawableElement {

	public WorldPoint first;
	public WorldPoint second;
	public WorldPoint third;
	public int numSegments;
	private ArrayList<Line> lines;
	private transient Rectangle2D.Float boundingBox;
	private transient boolean selected;
	private transient WorldPoint selectedPoint;
	private transient float zoomFactor;
	
	public QuadraticBezierCurve(final WorldPoint firstPoint, final int numSegments) {
		
		this.first = new WorldPoint(firstPoint);
		this.lines = new ArrayList<Line>();
		this.numSegments = numSegments;
		this.selected = false;
		this.selectedPoint = null;
		this.zoomFactor = 0.0f;
	}
	
	public QuadraticBezierCurve(final QuadraticBezierCurve curve) {
		
		this.first = new WorldPoint(curve.first);
		this.second = new WorldPoint(curve.second);
		this.third = new WorldPoint(curve.third);
		this.numSegments = curve.numSegments;
		this.lines = new ArrayList<Line>();
		this.selected = false;
		this.zoomFactor = 0.0f;
		
		// re-create the lines and bounding box...
		init();
	}
	
	public void addPoint(final WorldPoint point) {
		
		if(first == null) {
			
			first = new WorldPoint(point);
		} else if(second == null) {
			
			second = new WorldPoint(point);
		} else if(third == null) {
			
			third = new WorldPoint(point);
		} else {
			
			System.err.println("Oops, tried to add a point to a completed curve.");
			return;
		}
		
		init();
	}
	
	public void setNumSegments(final int numSegments) {
		
		this.numSegments = numSegments;
		init();
	}
	
	public int pointsCount() {
		
		int numPoints = 3;
		
		if(first == null) {
			
			numPoints--;
		}
		
		if(second == null) {
			
			numPoints--;
		}
		
		if(third == null) {
			
			numPoints--;
		}
		
		return numPoints;
	}
	
	public void init() {
		
		if(pointsCount() != 3 || numSegments <= 0) {
			
			return;
		}
		
		// clear out old list
		lines.clear();
		
		WorldPoint temp1, temp2, temp3;
		WorldPoint point1 = new WorldPoint(0.0f, 0.0f);
		WorldPoint point2 = new WorldPoint(0.0f, 0.0f);
		float nextTt = 0.0f;
		
		// equation for quadratic bezier curve: (1 - t)^2 * P0 + 2 * (1 - t)t * P1 + t^2 * P2
		float increment = 1.0f / (float)numSegments;
		for(int ii = 0; ii < numSegments; ii++) {
			
			float tt = ii * increment;
			
			// only need to calculate the first point on the first iteration
			if(ii == 0) {
				
				temp1 = first.mul((float)Math.pow((1.0f - tt), 2));
				temp2 = second.mul(2.0f * (1.0f - tt) * tt);
				temp3 = third.mul(tt * tt);
				
				point1.x = temp1.x + temp2.x + temp3.x;
				point1.y = temp1.y + temp2.y + temp3.y;
			} else { // we can use the previous point
				
				point1.x = point2.x;
				point1.y = point2.y;
			}
			
			nextTt = tt + increment;
			
			temp1 = first.mul((float)Math.pow((1.0f - nextTt), 2));
			temp2 = second.mul(2.0f * (1.0f - nextTt) * nextTt);
			temp3 = third.mul(nextTt * nextTt);
			
			point2.x = temp1.x + temp2.x + temp3.x;
			point2.y = temp1.y + temp2.y + temp3.y;
			
			lines.add(new Line(point1, point2));
		}
		
		first.init();
		second.init();
		third.init();
		
		// initialize the bounding box
		initBoundingBox();
	}
	
	private void initBoundingBox() {
		
		boundingBox = null;
		if(first != null && second != null && third != null) {
		
			float minX = Math.min(Math.min(first.x, second.x), third.x);
			float minY = Math.min(Math.min(first.y, second.y), third.y);
			float maxX = Math.max(Math.max(first.x, second.x), third.x);
			float maxY = Math.max(Math.max(first.y, second.y), third.y);
			boundingBox = new Rectangle2D.Float(minX * zoomFactor, 
					minY * zoomFactor, 
					Math.abs(maxX - minX) * zoomFactor, 
					Math.abs(maxY - minY) * zoomFactor);
			
			if(first.boundingBox != null) {
				
				boundingBox = (Float)boundingBox.createUnion(first.boundingBox);
			}
			
			if(second.boundingBox != null) {
				
				boundingBox = (Float)boundingBox.createUnion(second.boundingBox);
			}
			
			if(third.boundingBox != null) {
			
				boundingBox = (Float)boundingBox.createUnion(third.boundingBox);
			}
		}
	}
	
	public ArrayList<Line> getLines() {
		
		return lines;
	}
	
	@Override
	public void draw(Graphics gfx, final float zoomFactor) {
		
		Color temp = gfx.getColor();
		if(selected) {
			
			gfx.setColor(Globals.SELECTED_COLOR);
		}
		
		if(first != null) {
			
			first.draw(gfx, zoomFactor);
		}
		
		// draw the curve without the intermediate points
		for(Line line : lines) {
			
			line.childDraw(gfx, zoomFactor);
		}
		
		if(second != null) {
			
			second.draw(gfx, zoomFactor);
		}
		
		if(third != null) {
			
			third.draw(gfx, zoomFactor);
		}
		
		// for testing...
//		if(boundingBox != null) {
//			
//			gfx.drawRect(Math.round(boundingBox.x), 
//					Math.round(boundingBox.y), 
//					Math.round(boundingBox.width), 
//					Math.round(boundingBox.height));
//		}
		
		// draw some helper lines for the curve
		gfx.setColor(Color.LIGHT_GRAY);
		if(first != null && second != null) {
			
			gfx.drawLine(Math.round(first.x * zoomFactor), 
					Math.round(first.y * zoomFactor), 
					Math.round(second.x * zoomFactor), 
					Math.round(second.y * zoomFactor));
			
			if(third != null) {

				gfx.drawLine(Math.round(second.x * zoomFactor), 
						Math.round(second.y * zoomFactor), 
						Math.round(third.x * zoomFactor), 
						Math.round(third.y * zoomFactor));
			}
		}
		
		gfx.setColor(temp);
		
		// if zoom factor has changed, update our bounding box
		if(this.zoomFactor != zoomFactor) {
		
			this.zoomFactor = zoomFactor;
			initBoundingBox();
		}
	}
	
	@Override
	public boolean hitTest(final int x, final int y) {
		
		if(pointsCount() != 3 || numSegments <= 0 || boundingBox == null) {
			
			return false;
		}
		
		this.selectedPoint = null;
		
		// if we are within the bounding box, check if we clicked any of the points or the lines
		if(boundingBox.contains(x, y)) {
			
			if(first.hitTest(x, y) || second.hitTest(x, y) || third.hitTest(x, y)) {
				
				if(first.hitTest(x, y)) {
					
					this.selectedPoint = first;
				} else if(second.hitTest(x, y)) {
					
					this.selectedPoint = second;
				} else {
					
					this.selectedPoint = third;
				}
				return true;
			}
			
			for(Line line : lines) {
				
				if(line.hitTest(x, y)) {
					
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public PropertiesPanel getPropertiesPanel() {
		
		return new CurvePanel(this);
	}

	@Override
	public void setSelected(boolean selected) {
		
		this.selected = selected;
	}

	@Override
	public WorldPoint getSelectedPoint() {
		
		return this.selectedPoint;
	}
}
