package com.deaddropgames.editor.pickle;

import com.badlogic.gdx.utils.Array;
import com.deaddropgames.editor.elements.IDrawableElement;

public class Level {

    private String name;
    private String description;
    private String author;
    private int revision;
    private int difficulty;
    private Array terrainGroups;
    private Array objects;

    public Level() {

        name = "";
        description = "";
        author = "";
        revision = 1;
        difficulty = 0;
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

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public String getAuthor() {

        return author;
    }

    public void setAuthor(String author) {

        this.author = author;
    }

    public int getRevision() {

        return revision;
    }

    public void setRevision(int revision) {

        this.revision = revision;
    }

    public int getDifficulty() {

        return difficulty;
    }

    public void setDifficulty(int difficulty) {

        this.difficulty = difficulty;
    }

    public Array getTerrainGroups() {

        return terrainGroups;
    }

    public Array getObjects() {

        return objects;
    }

}
