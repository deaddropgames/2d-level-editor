package ca.squadcar.games.editor;

import java.awt.Graphics;
import java.util.ArrayList;

public class PolyLine extends DrawableElement {

	private ArrayList<WorldPoint> points;
	private int pointSize;
	
	public PolyLine(final WorldPoint point) {
		
		points = new ArrayList<WorldPoint>();
		points.add(point);
		
		pointSize = 4;
	}
	
	public PolyLine(final PolyLine polyLine) {
		
		points = new ArrayList<WorldPoint>();
		for(WorldPoint point : polyLine.getPoints()) {
			
			points.add(new WorldPoint(point));
		}
		
		pointSize = 4;
	}
	
	// need to scale this properly for zoom and what not
	public void addPoint(WorldPoint point) {
	
		points.add(point);
	}
	
	@Override
	public void draw(Graphics gfx, float zoomFactor) {

		if(points.size() == 1) {
			
			// just draw the single point
			gfx.drawOval(Math.round(points.get(0).x * zoomFactor) - pointSize / 2, Math.round(points.get(0).y * zoomFactor) - pointSize / 2, pointSize, pointSize);
		} else if(points.size() > 1) {
		
			// get the points in array form
			int[] tempX = new int[points.size()];
			int[] tempY = new int[points.size()];
			for(int ii = 0; ii < points.size(); ii++) {
				
				tempX[ii] = Math.round(points.get(ii).x * zoomFactor);
				tempY[ii] = Math.round(points.get(ii).y * zoomFactor);
				gfx.drawOval(Math.round(points.get(ii).x * zoomFactor) - pointSize / 2, Math.round(points.get(ii).y * zoomFactor) - pointSize / 2, pointSize, pointSize);
			}
			
			gfx.drawPolyline(tempX, tempY, points.size());
		}
		
		super.draw(gfx, zoomFactor);
	}
	
	public ArrayList<WorldPoint> getPoints() {
		
		return points;
	}
}
