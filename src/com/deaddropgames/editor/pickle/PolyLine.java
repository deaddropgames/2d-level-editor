package com.deaddropgames.editor.pickle;

import com.deaddropgames.editor.elements.WorldPoint;

import java.util.ArrayList;

public class PolyLine {

    private ArrayList<WorldPoint> points;

    public PolyLine() {

        points = new ArrayList<WorldPoint>();
    }

    public ArrayList<WorldPoint> getPoints() {

        return points;
    }
}
