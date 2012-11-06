package ca.squadcar.games.editor;

public class JsonElement {

	public static final short TypeNone = 0;
	public static final short TypePoint = 1;
	public static final short TypeLine = 2;
	public static final short TypeCurve = 3;
	
	public short type;
	public WorldPoint[] points;
	public int numSegments; // this is really only needed for curves
	
	public JsonElement() {
		
		type = JsonElement.TypeNone;
		numSegments = 0;
	}
	
	public JsonElement(final WorldPoint point) {

		type = JsonElement.TypePoint;
		points = new WorldPoint[1];
		points[0] = new WorldPoint(point);
	}
	
	public JsonElement(final Line line) {
		
		type = JsonElement.TypeLine;
		points = new WorldPoint[2];
		points[0] = new WorldPoint(line.start);
		points[1] = new WorldPoint(line.end);
		numSegments = 1;
	}
	
	public JsonElement(final QuadraticBezierCurve curve) {

		type = JsonElement.TypeCurve;
		points = new WorldPoint[3];
		points[0] = new WorldPoint(curve.first);
		points[1] = new WorldPoint(curve.second);
		points[2] = new WorldPoint(curve.third);
		numSegments = curve.numSegments;
	}
	
	public IDrawableElement toDrawableElement() {
		
		IDrawableElement element = null;
		switch(type) {
		
			case JsonElement.TypePoint: {
				
				if(points != null && points.length == 1 && points[0] != null) {
					
					element = new WorldPoint(points[0]);
				}
				break;
			}
			case JsonElement.TypeLine: {
				
				if(points != null && points.length == 2 && points[0] != null && points[1] != null) {
					
					element = new Line(points[0], points[1]);
				}
				break;
			}
			case JsonElement.TypeCurve: {

				if(points != null && points.length == 3 && points[0] != null && points[1] != null && points[2] != null && numSegments > 0) {
					
					element = new QuadraticBezierCurve(points[0], numSegments);
					((QuadraticBezierCurve)element).addPoint(points[1]);
					((QuadraticBezierCurve)element).addPoint(points[2]);
				}
				break;
			}
			default:
			case JsonElement.TypeNone: {
				
				element = null;
				break;
			}
		}
		
		return element;
	}
}
