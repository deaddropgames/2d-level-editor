package ca.squadcar.games.editor;

public class WorldPoint {

	public float x;
	public float y;
	
	public WorldPoint() {
		
		x = 0.0f;
		y = 0.0f;
	}
	
	public WorldPoint(float x, float y) {
		
		this.x = x;
		this.y = y;
	}
	
	public WorldPoint(final WorldPoint point) {
		
		this.x = point.x;
		this.y = point.y;
	}
}
