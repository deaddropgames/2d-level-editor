package com.deaddropgames.editor;

import java.awt.Color;

public class Globals {

	// the size, in pixels, of a point's circle drawing
	final public static int POINT_SIZE = 8;
	
	// the absolute maximum of a spinner control's x/y
	final public static double SPINNER_EXTENT = 10000.0;
	
	// the increment value for a spinner control
	final public static double SPINNER_INC = 0.5;
	
	// the color we use to show which element is selected
	final public static Color SELECTED_COLOR = Color.BLUE;
	
	// max distance from a line to consider it being hit (clicked)
	final public static float HIT_TEST_DIST = 0.25f;
}
