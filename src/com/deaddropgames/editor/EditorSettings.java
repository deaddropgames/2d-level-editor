package com.deaddropgames.editor;

public class EditorSettings {

    public int canvasWidth;
    public int canvasHeight;
    public String simJar;
    public int numCurveSegments;

    public EditorSettings() {

        this.canvasWidth = 500;
        this.canvasHeight = 300;
        this.simJar = "";
        this.numCurveSegments = 10;
    }
}
