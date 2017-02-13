package com.deaddropgames.editor.pickle;


import com.badlogic.gdx.utils.Array;
import com.deaddropgames.editor.elements.Tree;

import java.util.ArrayList;

public class UploadLevel {

    private String name;
    private int difficulty;
    private String description;
    private float end_x;
    private GameData game_data;
    private EditorData editor_data;

    public UploadLevel(final ExportLevel exportLevel, final Level level) {

        name = level.getName();
        difficulty = level.getDifficulty();
        description = level.getDescription();
        end_x = level.getEndX();

        game_data = new GameData(exportLevel);
        editor_data = new EditorData(level);
    }

    public void setName(String name) {

        this.name = name;
    }

    public void setDifficulty(int difficulty) {

        this.difficulty = difficulty;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public void setEnd_x(float end_x) {

        this.end_x = end_x;
    }

    private class GameData {

        private ArrayList<PolyLine> polyLines;
        private ArrayList<Tree> trees;

        GameData(final ExportLevel level) {

            polyLines = level.getPolyLines();
            trees = level.getTrees();
        }
    }

    private class EditorData {

        private Array terrainGroups;
        private Array objects;

        EditorData(final Level level) {

            terrainGroups = level.getTerrainGroups();
            objects = level.getObjects();
        }
    }
}
