package com.deaddropgames.editor.elements;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;

import com.deaddropgames.editor.Globals;
import com.deaddropgames.editor.gui.PropertiesPanel;
import com.deaddropgames.editor.gui.TreePanel;

public class Tree implements IDrawableElement {

    public float width;
    public float height;
    public float trunkHeight;
    public int levels;
    public WorldPoint location;

    private transient Rectangle2D.Float boundingBox;
    private transient boolean selected;
    private transient float zoomFactor;
    private transient Rectangle2D.Float trunk;
    private transient Triangle [] triangles;

    public Tree() {}

    public Tree(final float width, final float height, final float trunkHeight, final int levels, final WorldPoint location) {

        this.width = width;
        this.height = height;
        this.trunkHeight = trunkHeight;
        this.levels = levels;
        this.location = new WorldPoint(location);

        init();
    }

    @Override
    public void init() {

        float trunkWidth = 0.3f * width;

        trunk = new Rectangle2D.Float(location.x - trunkWidth * 0.5f,
                location.y - trunkHeight,
                trunkWidth,
                trunkHeight);

        if(levels > 0) {

            float levelHeight = height / (float)levels;
            float startWidth = width * 0.5f;
            float widthDec = (width - trunkWidth) / (float)levels * 0.65f;
            if(triangles == null || triangles.length != levels) {

                triangles = new Triangle[levels];
            }
            for(int ii = 0; ii < triangles.length; ii++) {

                float lower = trunkHeight + ii * levelHeight;
                float upper = lower + levelHeight;
                // adjust lower so it overlaps previous level
                if(ii > 0) {
                    lower -= 0.25f * levelHeight;
                }
                float halfWidth = startWidth - ii * widthDec;
                triangles[ii] = new Triangle(new WorldPoint(location.x + halfWidth, location.y - lower),
                        new WorldPoint(location.x - halfWidth, location.y - lower),
                        new WorldPoint(location.x + 0.0f, location.y - upper));
            }
        }

        if(zoomFactor != 0.0f) {

            initBoundingBox();
        }
    }

    @Override
    public boolean isTerrain() {

        return false;
    }

    private void initBoundingBox() {

        boundingBox = new Rectangle2D.Float(trunk.x * zoomFactor,
                trunk.y * zoomFactor,
                trunk.width * zoomFactor,
                trunk.height * zoomFactor);

        if(triangles != null) {

            for(Triangle triangle : triangles) {

                if(triangle.boundingBox != null) {

                    boundingBox = (Float)boundingBox.createUnion(triangle.boundingBox);
                }
            }
        }
    }

    @Override
    public void draw(Graphics gfx, float zoomFactor) {

        Color temp = gfx.getColor();
        if(selected) {

            gfx.setColor(Globals.SELECTED_COLOR);
        } else {

            gfx.setColor(new Color(0.361f, 0.122f, 0.118f));
        }

        // draw trunk
        gfx.drawRect(Math.round(trunk.x * zoomFactor),
                Math.round(trunk.y * zoomFactor),
                Math.round(trunk.width * zoomFactor),
                Math.round(trunk.height * zoomFactor));

        if(!selected) {

            gfx.setColor(new Color(0.133f, 0.424f, 0.008f));
        }

        // draw triangles
        if(triangles != null) {

            for(Triangle triangle : triangles) {

                triangle.draw(gfx, zoomFactor);
            }
        }

        gfx.setColor(temp);

        // if zoom factor has changed, update our bounding box
        if(this.zoomFactor != zoomFactor) {

            this.zoomFactor = zoomFactor;
            initBoundingBox();
        }

        // for testing
//		if(boundingBox != null) {
//			
//			gfx.drawRect(Math.round(boundingBox.x), 
//					Math.round(boundingBox.y), 
//					Math.round(boundingBox.width), 
//					Math.round(boundingBox.height));
//		}
    }

    @Override
    public boolean hitTest(int x, int y) {

        if(boundingBox == null) {

            return false;
        }

        if(boundingBox.contains(x, y)) {

            if(trunk.contains(x / zoomFactor, y / zoomFactor)) {

                return true;
            }

            if(triangles != null) {

                for(Triangle triangle : triangles) {

                    if(triangle.hitTest(x, y)) {

                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public PropertiesPanel getPropertiesPanel() {

        return new TreePanel(this);
    }

    @Override
    public void setSelected(boolean selected) {

        this.selected = selected;
    }

    @Override
    public WorldPoint getSelectedPoint() {

        return location;
    }
}
