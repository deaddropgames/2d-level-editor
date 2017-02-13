package com.deaddropgames.editor.pickle;

public class BaseLevel {

    protected String name;
    protected String description;
    protected String author;
    protected int difficulty;

    protected float endX;

    public BaseLevel() {

        name = "";
        description = "";
        author = "";
        difficulty = 0;
        endX = -1f;
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

    public int getDifficulty() {

        return difficulty;
    }

    public void setDifficulty(int difficulty) {

        this.difficulty = difficulty;
    }

    public float getEndX() {

        return endX;
    }

    public void setEndX(float endX) {

        this.endX = endX;
    }
}
