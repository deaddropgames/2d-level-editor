package com.deaddropgames.editor.pickle;

import com.badlogic.gdx.utils.Array;

public class ApiBaseList {

    private int count;
    private String next;
    private String previous;
    private Array results;

    public int getCount() {

        return count;
    }

    public String getNext() {

        return next;
    }

    public String getPrevious() {

        return previous;
    }

    public Array getResults() {

        return results;
    }
}
