package com.deaddropgames.editor.pickle;

import com.badlogic.gdx.utils.Json;
import com.deaddropgames.editor.elements.*;

public class Utils {

    private static Json json;

    public static Json getJsonizer() {

        if(json == null) {

            json = new Json();

            // add some class tags for our internal classes...
            json.addClassTag("point", WorldPoint.class);
            json.addClassTag("line", Line.class);
            json.addClassTag("curve", QuadraticBezierCurve.class);
            json.addClassTag("tree", Tree.class);
            json.addClassTag("triangle", Triangle.class);
        }

        return json;
    }
}
