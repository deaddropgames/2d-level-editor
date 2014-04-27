package com.deaddropgames.editor.export;

public class Level {

    public String name;
    public String description;
    public String author;
    public int revision;
    public int difficulty;
    public PolyLine[] polyLines;
    public TreeExport[] trees;

    public Level() {

        this.name = "";
        this.description = "";
        this.author = "";
        this.revision = 0;
        this.difficulty = 0;
    }
}
