package ca.squadcar.games.editor;

import java.awt.Graphics;
import java.util.ArrayList;

public class QuadraticBezierCurve implements IDrawableElement, IBoundingBox {

	private WorldPoint first;
	private WorldPoint second;
	private WorldPoint third;
	private ArrayList<Line> lines;
	private int numSegments;
	
	public QuadraticBezierCurve(final WorldPoint firstPoint, final int numSegments) {
		
		this.first = new WorldPoint(firstPoint);
		this.lines = new ArrayList<Line>();
		this.numSegments = numSegments;
	}
	
	public QuadraticBezierCurve(final QuadraticBezierCurve curve) {
		
		this.first = new WorldPoint(curve.first);
		this.second = new WorldPoint(curve.second);
		this.third = new WorldPoint(curve.third);
		this.numSegments = curve.numSegments;
		lines = new ArrayList<Line>(curve.lines);
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
	
	public void setFirstPoint(final WorldPoint point) {
		
		first = new WorldPoint(point);
		init();
	}
	
	public void setSecondPoint(final WorldPoint point) {
		
		second = new WorldPoint(point);
		init();
	}
	
	public void setThirdPoint(final WorldPoint point) {
		
		third = new WorldPoint(point);
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
	
	private void init() {
		
		if(pointsCount() != 3 || numSegments <= 0) {
			
			return;
		}
		
		// clear out old list
		lines.clear();
		
		WorldPoint temp1, temp2, temp3;
		WorldPoint point1 = new WorldPoint(0.0f, 0.0f);
		WorldPoint point2 = new WorldPoint(0.0f, 0.0f);
		float nextTt = 0.0f;
		
		float increment = 1.0f / (float)numSegments;
		for(float tt = 0; tt < 1.0f; tt += increment) {
			
			// equation: (1 - t)^2 * P0 + 2 * (1 - t)t * P1 + t^2 * P2
			temp1 = first.mul((float)Math.pow((1.0f - tt), 2));
			temp2 = second.mul(2.0f * (1.0f - tt) * tt);
			temp3 = third.mul(tt * tt);
			
			point1.x = temp1.x + temp2.x + temp3.x;
			point1.y = temp1.y + temp2.y + temp3.y;
			
			nextTt = tt + increment;
			
			temp1 = first.mul((float)Math.pow((1.0f - nextTt), 2));
			temp2 = second.mul(2.0f * (1.0f - nextTt) * nextTt);
			temp3 = third.mul(nextTt * nextTt);
			
			point2.x = temp1.x + temp2.x + temp3.x;
			point2.y = temp1.y + temp2.y + temp3.y;
			
			lines.add(new Line(point1, point2));
		}
		
		// add the final line segment ending at the third point
		lines.add(new Line(point2, third));
	}
	
	@Override
	public void draw(Graphics gfx, float zoomFactor) {
		
		for(Line line : lines) {
			
			line.draw(gfx, zoomFactor);
		}
		
		// draw the second point since it won't be on any of the lines
		if(second != null) {
			
			second.draw(gfx, zoomFactor);
		}
	}

	@Override
	public boolean containsPoint(float x, float y) {
		
		// TODO
		return false;
	}
	
	@Override
	public boolean hitTest(int x, int y, float zoomFactor) {
		
		if(first == null || second == null || third == null) {
			
			return false;
		}
		
		// TODO
		return false;
	}
}
