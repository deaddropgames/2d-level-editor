package ca.squadcar.games.editor;

public class EditorSettings {

	public int canvasWidth;
	public int canvasHeight;
	public String simJar;
	public String simArgs;
	public int numCurveSegments;
	
	public EditorSettings() {
		
		this.canvasWidth = 500;
		this.canvasHeight = 300;
		this.simJar = "";
		this.simArgs = "";
		this.numCurveSegments = 10;
	}
}
