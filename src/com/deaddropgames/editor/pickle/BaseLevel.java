package com.deaddropgames.editor.pickle;

public class BaseLevel {

    protected String name;
    protected String description;
    protected String author;
    protected int revision;
    protected int difficulty;

    public BaseLevel() {

        name = "";
        description = "";
        author = "";
        revision = 1;
        difficulty = 0;
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
}
