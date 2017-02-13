package com.deaddropgames.editor.pickle;

import com.badlogic.gdx.utils.Array;
import com.deaddropgames.editor.elements.IDrawableElement;

public class Level extends BaseLevel {

    private transient long id;
    private Array terrainGroups;
    private Array objects;

    public Level() {

        super();
        id = 0;
        terrainGroups = new Array();
        objects = new Array();
    }

    public void init() {

        for(Object objList : terrainGroups) {

            Array list = (Array)objList;
            for(Object objElem : list) {

                IDrawableElement element = (IDrawableElement)objElem;
                element.init();
            }
        }

        for(Object obj : objects) {

            IDrawableElement element = (IDrawableElement)obj;
            element.init();
        }
    }

    public boolean hasElements() {

        return terrainGroups.size > 0 || objects.size > 0;
    }

    public Array getTerrainGroups() {

        return terrainGroups;
    }

    public Array getObjects() {

        return objects;
    }

    public long getId() {

        return id;
    }
}
