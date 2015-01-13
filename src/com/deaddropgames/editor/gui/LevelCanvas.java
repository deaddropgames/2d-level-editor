package com.deaddropgames.editor.gui;

import java.awt.Graphics;

import javax.swing.JPanel;

import com.badlogic.gdx.utils.Array;
import com.deaddropgames.editor.elements.*;
import com.deaddropgames.editor.pickle.ExportLevel;
import com.deaddropgames.editor.pickle.PolyLine;
import com.deaddropgames.editor.pickle.Level;
import com.deaddropgames.editor.pickle.Utils;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class LevelCanvas extends JPanel {

    private Array currList;
    private float zoomFactor;
    private IDrawableElement temp;
    private Dimension canvasDim;
    private BipedReference bipedRef;
    private IDrawableElement lastHitElement;
    private Line guideLine; // line drawn from last point to mouse-moved point when in drawing mode
    private Level level;

    /**
     * Custom panel for drawing onto
     */
    public LevelCanvas() {

        setBackground(Color.WHITE);

        currList = null;
        zoomFactor = 10.0f;
        temp = null;
        canvasDim = null;
        bipedRef = new BipedReference();
        guideLine = null;

        level = new Level();
        lastHitElement = null;
    }

    @Override
    public void paint(Graphics gfx) {

        super.paint(gfx);

        // draw the terrain
        for(Object objList : level.getTerrainGroups()) {

            Array list = (Array)objList;
            for(Object objElem : list) {

                IDrawableElement element = (IDrawableElement)objElem;
                element.draw(gfx, zoomFactor);
            }
        }

        // draw the objects
        for(Object obj : level.getObjects()) {

            IDrawableElement element = (IDrawableElement)obj;
            element.draw(gfx, zoomFactor);
        }

        if(temp != null) {

            temp.draw(gfx, zoomFactor);
        }

        bipedRef.draw(gfx, zoomFactor);

        gfx.setColor(Color.LIGHT_GRAY);
        if(guideLine != null) {

            guideLine.draw(gfx, zoomFactor);
        }

        if(level.getEndX() > 0f) {

            int endX = Math.round(level.getEndX() * zoomFactor);
            int yMax = (int)Math.round(canvasDim.getHeight() * zoomFactor);
            gfx.setColor(Color.red);
            gfx.drawLine(endX, 0, endX, yMax);
        }
    }

    public void setCursor(int cursor) {

        setCursor(Cursor.getPredefinedCursor(cursor));
    }

    @SuppressWarnings("unchecked")
    public void addDrawableElement(final IDrawableElement element, boolean currElemIsNew) {

        if(currList == null) {

            currList = new Array();
        }

        if(element.isTerrain()) {

            if (currElemIsNew) {

                level.getTerrainGroups().add(currList);
            }

            currList.add(element);
        } else {

            level.getObjects().add(element);
        }
    }

    public void setCurrListForElement(final IDrawableElement element) {

        currList = findListForElement(element);
    }

    /**
     * This gets called in cases where we just started adding an element, but cancelled it before it was done..
     */
    @SuppressWarnings("unchecked")
    public void cancelCurrList() {

        if(currList != null && currList.size == 0) {

            level.getTerrainGroups().removeValue(currList, true);
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

        return level.hasElements();
    }

    public ExportLevel getLevelForExport() {

        // make sure there is something to export...
        if(!level.hasElements() || level.getTerrainGroups().size == 0) {

            return null;
        }

        ExportLevel exportLevel = new ExportLevel();
        exportLevel.copyMetaData(level);

        // all points are translated relative to the first upon save...
        IDrawableElement firstElem = (IDrawableElement)((Array)level.getTerrainGroups().get(0)).get(0);
        WorldPoint transPoint = getStartPoint(firstElem);
        WorldPoint currPoint;

        // we will export the terrain first
        for(Object objList : level.getTerrainGroups()) {

            Array list = (Array) objList;
            PolyLine polyLine = new PolyLine();
            boolean isFirst = true;
            for(Object objElem : list) {

                // we add the first point, and then add mid and end points for each successive chain
                IDrawableElement element = (IDrawableElement)objElem;
                if(isFirst) {

                    currPoint = new WorldPoint(getStartPoint(element));
                    currPoint.x -= transPoint.x;
                    currPoint.y -= transPoint.y;
                    currPoint.y *= -1.0f;
                    polyLine.getPoints().add(currPoint);
                    isFirst = false;
                }

                // we only export lines and curves as terrain
                if(element instanceof Line) {

                    Line line = (Line)element;
                    currPoint = new WorldPoint(line.end);
                    currPoint.x -= transPoint.x;
                    currPoint.y -= transPoint.y;
                    currPoint.y *= -1.0f;
                    polyLine.getPoints().add(new WorldPoint(currPoint));
                } else if(element instanceof QuadraticBezierCurve) {

                    QuadraticBezierCurve curve = (QuadraticBezierCurve)element;
                    for(Line line : curve.getLines()) {

                        currPoint = new WorldPoint(line.end);
                        currPoint.x -= transPoint.x;
                        currPoint.y -= transPoint.y;
                        currPoint.y *= -1.0f;
                        polyLine.getPoints().add(new WorldPoint(currPoint));
                    }
                }
            }

            // store the completed polyline - it must have at least 2 points to be valid...
            if(polyLine.getPoints().size() > 1) {

                exportLevel.getPolyLines().add(polyLine);
            }
        }

        // now let's export the objects (currently this is just trees...)
        for(Object obj : level.getObjects()) {

            IDrawableElement element = (IDrawableElement) obj;
            if(element instanceof Tree) {

                // add the tree, but don't forget to translate it relative to the first point...
                Tree tree = new Tree((Tree)element); // also make a copy so you don't clobber the original!!
                tree.location = new WorldPoint(tree.location.x - transPoint.x, -(tree.location.y - transPoint.y));
                exportLevel.getTrees().add(tree);
            }
        }

        return exportLevel;
    }

    public Level getLevel() {

        return level;
    }

    public void reset() {

        level = new Level();
        lastHitElement = null;
    }

    public boolean loadLevelFromFile(final File levelFile) throws IOException {

        level = Utils.getJsonizer().fromJson(Level.class, new FileReader(levelFile));
        level.init();

        return true;
    }

    public void updateForViewportChange(final Point point) {

        bipedRef.setOffset(point);
    }

    public boolean hitTest(final int x, final int y) {

        lastHitElement = null;

        // check if terrain was hit...
        for(Object objList : level.getTerrainGroups()) {

            Array list = (Array)objList;
            for(Object objElem : list) {

                IDrawableElement element = (IDrawableElement)objElem;
                if(element.hitTest(x, y)) {

                    lastHitElement = element;
                    return true;
                }
            }
        }

        // if not terrain, check if an object was hit...
        for(Object obj : level.getObjects()) {

            IDrawableElement element = (IDrawableElement)obj;
            if(element.hitTest(x, y)) {

                lastHitElement = element;
                return true;
            }
        }

        return false;
    }

    public IDrawableElement getLastHitElement() {

        return lastHitElement;
    }

    @SuppressWarnings("unchecked")
    public void updateNeighbors(final IDrawableElement element) {

        Array list = findListForElement(element);
        if(list == null) {

            // shouldn't get here...
            return;
        }

        int index = list.indexOf(element, true);
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

                setEndPoint(point, (IDrawableElement)list.get(prev));
            }
        }

        // update the next neighbor...
        if(next < list.size) {

            // get the point that we need to update
            WorldPoint point = getEndPoint(element);

            // update the appropriate point on the neighbor
            if(point != null) {

                setStartPoint(point, (IDrawableElement)list.get(next));
            }
        }
    }

    public void selectNone() {

        for(Object objList : level.getTerrainGroups()) {

            Array list = (Array)objList;
            for(Object objElem : list) {

                IDrawableElement element = (IDrawableElement)objElem;
                element.setSelected(false);
            }
        }

        for(Object obj : level.getObjects()) {

            IDrawableElement element = (IDrawableElement)obj;
            element.setSelected(false);
        }

        lastHitElement = null;
    }

    @SuppressWarnings("unchecked")
    public void deleteElement(IDrawableElement element) {

        Array list = findListForElement(element);
        if(list == null) {

            // shouldn't get here...
            return;
        }

        int index = list.indexOf(element, true);
        if(index == -1) {

            // shouldn't get here...
            return;
        }

        // special handling for objects...
        if(level.getObjects() == list) {

            level.getObjects().removeIndex(index);
        } else {

            // check if removing the first element
            if(index == 0) {

                // if it just the start point, delete it
                // otherwise do nothing, since we need the start point...
                if(list.size == 1) {

                    list.clear();
                    level.getTerrainGroups().removeValue(list, true);
                } else {

                    list.removeIndex(index);
                }
            } else {

                boolean isLast = (index == (list.size - 1));

                // if its the last element, remove it
                if(isLast) {

                    list.removeIndex(index);
                } else { // otherwise we need to update the next element

                    setStartPoint(getStartPoint(element), (IDrawableElement)list.get(index + 1));
                    list.removeIndex(index);
                }
            }
        }
    }

    public void addEnd(float endX) {

        level.setEndX(endX);
    }

    /**
     * Finds large terrain angles in order to smooth them
     * @param minAngle the minimum angle to consider the angle large
     * @return integer of the number of eligible terrain angles found
     */
    public int findLargeTerrainAngles(float minAngle) {

        int numFound = 0;
        for(Object objList : level.getTerrainGroups()) {

            IDrawableElement first, second = null;
            float angle;
            Array list = (Array)objList;
            for(Object objElem : list) {

                first = second;
                second = (IDrawableElement)objElem;
                if(first != null) {

                    angle = getAngleBetweenElements(first, second, null);
                    if(angle > minAngle) {

                        numFound++;
                    }
                }
            }
        }

        return numFound;
    }

    /**
     * Smooths out any terrain angles greater than minAngle by clipPercent on each attached element
     * @param minAngle the minimum angle to apply smoothing to
     * @param clipPercent the percent with which to clip each attached element
     */
    @SuppressWarnings("unchecked")
    public void smoothLargeTerrainAngles(float minAngle, float clipPercent) {

        for(Object objList : level.getTerrainGroups()) {

            IDrawableElement first, second = null;
            float angle;
            Array list = (Array)objList;
            // keep track of the lines we might have to insert at the end
            ArrayList<Integer> indices = new ArrayList<Integer>();
            ArrayList<Line> insertLines = new ArrayList<Line>();
            int index = 0;
            for(Object objElem : list) {

                first = second;
                second = (IDrawableElement)objElem;
                if(first != null) {

                    ArrayList<Line> lines = new ArrayList<Line>();
                    angle = getAngleBetweenElements(first, second, lines);
                    if(angle > minAngle) {

                        // clip the lines
                        lines.get(0).clip(clipPercent, false);
                        lines.get(1).clip(clipPercent, true);

                        // insert a new line between them
                        // NOTE: we can't do this in the loop, we do it at the end
                        indices.add(index);
                        insertLines.add(new Line(new WorldPoint(lines.get(0).end),
                                new WorldPoint(lines.get(1).start)));
                    }
                }

                index++;
            }

            for(int ii = 0; ii < indices.size(); ii++) {

                list.insert(indices.get(ii), insertLines.get(ii));
                for(int jj = ii + 1; jj < indices.size(); jj++) {

                    // every time we insert, we have to make sure any further inserts are also incremented
                    indices.set(jj, indices.get(jj) + 1);
                }
            }
        }
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
            element.init();
        } else if(element instanceof QuadraticBezierCurve) {

            ((QuadraticBezierCurve)element).first.x = point.x;
            ((QuadraticBezierCurve)element).first.y = point.y;
            element.init();
        }
    }

    private static void setEndPoint(final WorldPoint point, IDrawableElement element) {

        if(element instanceof WorldPoint) {

            ((WorldPoint)element).x = point.x;
            ((WorldPoint)element).y = point.y;
        } else if(element instanceof Line) {

            ((Line)element).end.x = point.x;
            ((Line)element).end.y = point.y;
            element.init();
        } else if(element instanceof QuadraticBezierCurve) {

            ((QuadraticBezierCurve)element).third.x = point.x;
            ((QuadraticBezierCurve)element).third.y = point.y;
            element.init();
        }
    }

    @SuppressWarnings("unchecked")
    private Array findListForElement(IDrawableElement element) {

        for(Object objList : level.getTerrainGroups()) {

            Array list = (Array)objList;
            if(list.indexOf(element, true) >= 0) {

                return list;
            }
        }

        if(level.getObjects().indexOf(element, true) >= 0) {

            return level.getObjects();
        }

        return null;
    }

    /**
     * Calculates the connection angle between the first and second element, measured in a clockwise fashion from first
     * to second
     * @param first a curve or line
     * @param second a curve or line
     * @param lines a list that will contain the found lines, or null if unneeded
     * @return the angle in degrees or a negative value if the calculation isn't possible or there's an error
     */
    private float getAngleBetweenElements(IDrawableElement first, IDrawableElement second, ArrayList<Line> lines) {

        // we can only do the calculation between lines, curves and lines and curves
        if(!(first instanceof Line || first instanceof QuadraticBezierCurve) ||
                !(second instanceof Line || second instanceof QuadraticBezierCurve)) {

            return -1f;
        }

        // sanity check...
        if((first instanceof QuadraticBezierCurve && ((QuadraticBezierCurve)first).getLines().size() == 0) ||
                (second instanceof QuadraticBezierCurve && ((QuadraticBezierCurve)second).getLines().size() == 0)) {

            return -1f;
        }

        QuadraticBezierCurve curve;
        Line firstLine, secondLine;

        // convert our elements to lines...if it's a curve, use the last line if it's the first element, or the first
        // line if it's the second element
        if(first instanceof Line) {

            firstLine = (Line)first;
        } else { // first instanceof QuadraticBezierCurve

            curve = (QuadraticBezierCurve)first;
            firstLine = curve.getLines().get(curve.getLines().size() - 1);
        }

        if(second instanceof Line) {

            secondLine = (Line)second;
        } else { // second instanceof QuadraticBezierCurve

            curve = (QuadraticBezierCurve)second;
            secondLine = curve.getLines().get(0);
        }

        if(lines != null) {

            lines.add(firstLine);
            lines.add(secondLine);
        }

        return 180f - secondLine.toVector().angle(firstLine.toVector());
    }
}
