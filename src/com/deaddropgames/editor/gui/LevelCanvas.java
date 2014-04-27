package com.deaddropgames.editor.gui;

import java.awt.Graphics;

import javax.swing.JPanel;

import com.badlogic.gdx.utils.Array;
import com.deaddropgames.editor.elements.BipedReference;
import com.deaddropgames.editor.elements.IDrawableElement;
import com.deaddropgames.editor.elements.Line;
import com.deaddropgames.editor.elements.QuadraticBezierCurve;
import com.deaddropgames.editor.elements.WorldPoint;
import com.deaddropgames.editor.export.Level;
import com.deaddropgames.editor.pickle.Utils;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class LevelCanvas extends JPanel {

    private Array currList;
    private float zoomFactor;
    private IDrawableElement temp;
    private Dimension canvasDim;
    private BipedReference bipedRef;
    private IDrawableElement lastHitElement;
    private Line guideLine; // line drawn from last point to mouse-moved point when in drawing mode
    private com.deaddropgames.editor.pickle.Level pickleLevel;

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

        pickleLevel = new com.deaddropgames.editor.pickle.Level();
        lastHitElement = null;
    }

    @Override
    public void paint(Graphics gfx) {

        super.paint(gfx);

        // draw the terrain
        for(Object objList : pickleLevel.getTerrainGroups()) {

            Array list = (Array)objList;
            for(Object objElem : list) {

                IDrawableElement element = (IDrawableElement)objElem;
                element.draw(gfx, zoomFactor);
            }
        }

        // draw the objects
        for(Object obj : pickleLevel.getObjects()) {

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

                pickleLevel.getTerrainGroups().add(currList);
            }

            currList.add(element);
        } else {

            pickleLevel.getObjects().add(element);
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

            pickleLevel.getTerrainGroups().removeValue(currList, true);
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

        return pickleLevel.hasElements();
    }

    public Level getLevelForExport() {

        // TODO: need refactor when we get here...
        /*if(elementLists.size() == 0) {

            return null;
        }

        Level level = new Level();

        int numPolylines = elementLists.size();
        if(treeList != null) {

            numPolylines--;
        }

        level.polyLines = new com.deaddropgames.editor.export.PolyLine[numPolylines];

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
            level.polyLines[ii] = new com.deaddropgames.editor.export.PolyLine();
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
                tree.trunkHeight = elem.trunkHeight;
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
        */
        return null;
    }

    public com.deaddropgames.editor.pickle.Level getLevel() {

        return pickleLevel;
    }

    public void reset() {

        pickleLevel = new com.deaddropgames.editor.pickle.Level();
        lastHitElement = null;
    }

    public boolean loadLevelFromFile(final File levelFile) throws IOException {

        pickleLevel = Utils.getJsonizer().fromJson(com.deaddropgames.editor.pickle.Level.class,
                new FileReader(levelFile));
        pickleLevel.init();

        return true;
    }

    public void updateForViewportChange(final Point point) {

        bipedRef.setOffset(point);
    }

    public boolean hitTest(final int x, final int y) {

        lastHitElement = null;

        // check if terrain was hit...
        for(Object objList : pickleLevel.getTerrainGroups()) {

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
        for(Object obj : pickleLevel.getObjects()) {

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

        for(Object objList : pickleLevel.getTerrainGroups()) {

            Array list = (Array)objList;
            for(Object objElem : list) {

                IDrawableElement element = (IDrawableElement)objElem;
                element.setSelected(false);
            }
        }

        for(Object obj : pickleLevel.getObjects()) {

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
        if(pickleLevel.getObjects() == list) {

            pickleLevel.getObjects().removeIndex(index);
        } else {

            // check if removing the first element
            if(index == 0) {

                // if it just the start point, delete it
                // otherwise do nothing, since we need the start point...
                if(list.size == 1) {

                    list.clear();
                    pickleLevel.getTerrainGroups().removeValue(list, true);
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

        for(Object objList : pickleLevel.getTerrainGroups()) {

            Array list = (Array)objList;
            if(list.indexOf(element, true) >= 0) {

                return list;
            }
        }

        if(pickleLevel.getObjects().indexOf(element, true) >= 0) {

            return pickleLevel.getObjects();
        }

        return null;
    }
}
