package com.deaddropgames.editor;

import java.util.ArrayList;

import com.deaddropgames.editor.elements.IDrawableElement;
import com.deaddropgames.editor.elements.Line;
import com.deaddropgames.editor.elements.QuadraticBezierCurve;
import com.deaddropgames.editor.elements.Tree;
import com.deaddropgames.editor.elements.WorldPoint;

public class JsonLevel {

	public String name;
	public String description;
	public String author;
	public int revision;
	public int difficulty;
	public JsonElementList[] elementLists;
	
	public JsonLevel() {
		
		name = "";
		description = "";
		author = "";
		revision = 1;
		difficulty = 0;
	}
	
	public JsonLevel(ArrayList<ArrayList<IDrawableElement>> lists, final JsonLevel level) {
		
		if(level == null) {
			
			name = "";
			description = "";
			author = "";
			revision = 1;
			difficulty = 0;
		} else {
			
			name = level.name;
			description = level.description;
			author = level.author;
			revision = level.revision;
			difficulty = level.difficulty;
		}
		
		elementLists = new JsonElementList[lists.size()];
		ArrayList<IDrawableElement> currList = null;
		IDrawableElement element = null;
		for(int ii = 0; ii < lists.size(); ii++) {
			
			currList = lists.get(ii);
			elementLists[ii] = new JsonElementList(currList.size());
			for(int jj = 0; jj < currList.size(); jj++) {
				
				element = currList.get(jj);
				if(element instanceof WorldPoint) {
					
					elementLists[ii].elements[jj] = new JsonElement((WorldPoint)element);
				} else if(element instanceof Line) {
					
					elementLists[ii].elements[jj] = new JsonElement((Line)element);
				} else if(element instanceof QuadraticBezierCurve) {

					elementLists[ii].elements[jj] = new JsonElement((QuadraticBezierCurve)element);
				} else if(element instanceof Tree) {
					
					elementLists[ii].elements[jj] = new JsonElement((Tree)element);
				}
			}
		}
	}
}
