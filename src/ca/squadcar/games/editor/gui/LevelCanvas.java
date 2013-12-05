package ca.squadcar.games.editor.gui;

import java.awt.Graphics;

import javax.swing.JPanel;

import ca.squadcar.games.editor.JsonElement;
import ca.squadcar.games.editor.JsonElementList;
import ca.squadcar.games.editor.JsonLevel;
import ca.squadcar.games.editor.elements.BipedReference;
import ca.squadcar.games.editor.elements.IDrawableElement;
import ca.squadcar.games.editor.elements.Line;
import ca.squadcar.games.editor.elements.QuadraticBezierCurve;
import ca.squadcar.games.editor.elements.Tree;
import ca.squadcar.games.editor.elements.WorldPoint;
import ca.squadcar.games.editor.export.Level;
import ca.squadcar.games.editor.export.TreeExport;

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

	private ArrayList<ArrayList<IDrawableElement>> elementLists;
	private ArrayList<IDrawableElement> currList;
	private ArrayList<IDrawableElement> treeList;
	private float zoomFactor;
	private IDrawableElement temp;
	private Dimension canvasDim;
	private BipedReference bipedRef;
	private IDrawableElement lastHitElement;
	private JsonLevel level;
	private Line guideLine; // line drawn from last point to mouse-moved point when in drawing mode
	
	/**
	 * Custom panel for drawing onto
	 */
	public LevelCanvas() {
		
		setBackground(Color.WHITE);
		
		elementLists = new ArrayList<ArrayList<IDrawableElement>>();
		currList = null;
		zoomFactor = 10.0f;
		temp = null;
		canvasDim = null;
		bipedRef = new BipedReference();
		lastHitElement = null;
		guideLine = null;
		treeList = null;
		
		reset();
	}

	@Override
	public void paint(Graphics gfx) {
		
		super.paint(gfx);
		
		for(ArrayList<IDrawableElement> list : elementLists) {
			
			for(IDrawableElement element : list) {
				
				element.draw(gfx, zoomFactor);
			}
		}
		
		if(temp != null) {
			
			temp.draw(gfx, zoomFactor);
		}
		
		bipedRef.draw(gfx, zoomFactor);
		
		gfx.setColor(Color.LIGHT_GRAY);
		if(guideLine != null) {
			
			guideLine.draw(gfx, zoomFactor);
		}
	}
	
	public void setCursor(int cursor) {
		
		setCursor(Cursor.getPredefinedCursor(cursor));
	}
	
	public void addDrawableElement(final IDrawableElement element, boolean currElemIsNew) {
		
		if(currElemIsNew) {
			
			currList = new ArrayList<IDrawableElement>();
			elementLists.add(currList);
		}
		
		currList.add(element);
	}
	
	public void addTree(final Tree tree) {
		
		if(treeList == null) {
			
			treeList = new ArrayList<IDrawableElement>();
			elementLists.add(treeList);
		}
		treeList.add(tree);
	}
	
	public void setCurrListForElement(final IDrawableElement element) {
		
		currList = findListForElement(element);
	}
	
	/**
	 * This gets called in cases where we just started adding an element, but cancelled it before it was done..
	 */
	public void cancelCurrList() {
		
		if(currList != null && currList.size() == 0) {
			
			elementLists.remove(currList);
		}
		currList = null;
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
	
	/**
	 * The guideline will show on mouse movement when in drawing mode
	 * @param guideLine: the temporary guideline to draw
	 */
	public void setGuideLine(Line guideLine) {
		
		this.guideLine = guideLine;
	}
	
	public void setCanvasDimension(Dimension dim) {
		
		canvasDim = new Dimension(dim);
	}
	
	public boolean hasElements() {
		
		return (elementLists.size() > 0);
	}
	
	public Level getLevelForExport() {
		
		if(elementLists.size() == 0) {
			
			return null;
		}
	
		Level level = new Level();
		
		int numPolylines = elementLists.size();
		if(treeList != null) {
			
			numPolylines--;
		}
		
		level.polyLines = new ca.squadcar.games.editor.export.PolyLine[numPolylines];
		
		// we need to translate all points relative to the first
		IDrawableElement firstElem = elementLists.get(0).get(0);
		WorldPoint transPoint = getStartPoint(firstElem);
		ArrayList<WorldPoint> points = new ArrayList<WorldPoint>();
		WorldPoint currPoint;
		for(int ii = 0; ii < elementLists.size(); ii++) {
			
			// we handle the trees last
			if(elementLists.get(ii) == treeList) {
				
				continue;
			}
			
			points.clear();
			level.polyLines[ii] = new ca.squadcar.games.editor.export.PolyLine();
			boolean isFirst = true;
			for(IDrawableElement element : elementLists.get(ii)) {
				
				// we add the first point, and then add mid and end points for each successive chain
				if(isFirst) {
						
					currPoint = new WorldPoint(getStartPoint(element));
					currPoint.x -= transPoint.x;
					currPoint.y -= transPoint.y;
					currPoint.y *= -1.0f;
					points.add(currPoint);
					isFirst = false;
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
				
				continue;
			}
			
			level.polyLines[ii].points = new WorldPoint[points.size()];
			points.toArray(level.polyLines[ii].points);
		}
		
		// add the trees
		if(treeList != null && treeList.size() > 0) {
			
			ArrayList<TreeExport> trees = new ArrayList<TreeExport>();
			for(IDrawableElement treeElem : treeList) {
				
				Tree elem = ((Tree)treeElem);
				TreeExport tree = new TreeExport();
				tree.height = elem.height;
				tree.width = elem.width;
				tree.levels = elem.levels;
				tree.location = new WorldPoint(elem.location.x - transPoint.x, -(elem.location.y - transPoint.y));
				trees.add(tree);
			}
			
			level.trees = new TreeExport[trees.size()];
			trees.toArray(level.trees);
		} else {
			
			level.trees = new TreeExport[0];
		}
		
		return level;
	}
	
	public JsonLevel getLevelForSave() {
		
		if(elementLists.size() == 0) {
			
			return null;
		}

		JsonLevel level = new JsonLevel(elementLists, this.level);
		return level;
	}
	
	public void reset() {
		
		elementLists.clear();
		level = null;
		lastHitElement = null;
	}
	
	public boolean loadLevelFromFile(final File levelFile) throws IOException {
		
		level = null;
		
		BufferedReader br = null;
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
			level = gson.fromJson(sb.toString(), JsonLevel.class);
		} catch(Exception ex) {
			
			ex.printStackTrace();
			return false;
		} finally {
			
			if(br != null) {
				
				br.close();
			}
		}
		
		if(level == null) {
			
			return false;
		}
		
		for(JsonElementList list : level.elementLists) {
		
			currList = new ArrayList<IDrawableElement>();
			elementLists.add(currList);
			for(JsonElement jsonElement : list.elements) {
				
				IDrawableElement element = jsonElement.toDrawableElement();
				if(element == null) {
					
					return false;
				}
				
				currList.add(element);
			}
		}
		
		return true;
	}
	
	public Dimension getCanvasDimension() {
		
		return canvasDim;
	}
	
	public void updateForViewportChange(final Point point) {
		
		bipedRef.setOffset(point);
	}
	
	public boolean hitTest(final int x, final int y) {

		lastHitElement = null;
		
		// convert the mouse point to its world point
		for(ArrayList<IDrawableElement> list : elementLists) {
			
			for(IDrawableElement element : list) {
				
				if(element.hitTest(x, y)) {
					
					lastHitElement = element;
					return true;
				}
			}
		}
		
		return false;
	}
	
	public IDrawableElement getLastHitElement() {
		
		return lastHitElement;
	}
	
	public void updateNeighbors(final IDrawableElement element) {
		
		ArrayList<IDrawableElement> list = findListForElement(element);
		if(list == null) {
			
			// shouldn't get here...
			return;
		}
		
		int index = list.indexOf(element);
		if(index == -1) {
			
			// shouldn't get here...
			return;
		}
		
		int prev = index - 1;
		int next = index + 1;
		
		// update the previous neighbor...
		if(prev >= 0) {
			
			// get the point that we need to update
			WorldPoint point = getStartPoint(element);
			
			// update the appropriate point on the neighbor
			if(point != null) {
				
				setEndPoint(point, list.get(prev));
			}
		}
	
		// update the next neighbor...
		if(next < list.size()) {
			
			// get the point that we need to update
			WorldPoint point = getEndPoint(element);
			
			// update the appropriate point on the neighbor
			if(point != null) {
				
				setStartPoint(point, list.get(next));
			}
		}
	}
	
	public void selectNone() {
	
		for(ArrayList<IDrawableElement> list : elementLists) {
			
			for(IDrawableElement element : list) {
				
				element.setSelected(false);
			}
		}
		
		lastHitElement = null;
	}
	
	public void deleteElement(IDrawableElement element) {
		
		ArrayList<IDrawableElement> list = findListForElement(element);
		if(list == null) {
			
			// shouldn't get here...
			return;
		}
		
		int index = list.indexOf(element);
		if(index == -1) {
			
			// shouldn't get here...
			return;
		}
		
		// special handling for tree list...
		if(treeList == list) {
			
			treeList.remove(index);
			if(treeList.size() == 0) {
				
				elementLists.remove(treeList);
				treeList = null;
			}
		} else {
		
			// check if removing the first element
			if(index == 0) {
				
				// if it just the start point, delete it
				// otherwise do nothing, since we need the start point...
				if(list.size() == 1) {
					
					list.clear();
					elementLists.remove(list);
				} else {
					
					list.remove(index);
				}
			} else {
	
				boolean isLast = (index == (list.size() - 1));
				
				// if its the last element, remove it
				if(isLast) {
					
					list.remove(index);
				} else { // otherwise we need to update the next element
					
					setStartPoint(getStartPoint(element), list.get(index + 1));
					list.remove(index);
				}
			}
		}
		
		return;
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
			((Line)element).init();
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
			((Line)element).init();
		} else if(element instanceof QuadraticBezierCurve) {
			
			((QuadraticBezierCurve)element).third.x = point.x;
			((QuadraticBezierCurve)element).third.y = point.y;
			((QuadraticBezierCurve)element).init();
		}
	}
	
	private ArrayList<IDrawableElement> findListForElement(IDrawableElement element) {
		
		for(ArrayList<IDrawableElement> list : elementLists) {
			
			if(list.indexOf(element) >= 0) {
				
				return list;
			}
		}
		
		return null;
	}
}
