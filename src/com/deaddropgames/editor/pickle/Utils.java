package com.deaddropgames.editor.pickle;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.deaddropgames.editor.elements.*;

public class Utils {

    private static Json json;

    public static Json getJsonizer() {

        if(json == null) {

            json = new Json(JsonWriter.OutputType.json);
            json.setIgnoreUnknownFields(true);

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
