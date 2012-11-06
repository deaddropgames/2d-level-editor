package ca.squadcar.games.editor;

public class JsonLevel {

	public String name;
	public String description;
	public String author;
	public int revision;
	public int difficulty;
	public JsonElement[] elements;
	
	public JsonLevel() {
		
		name = "";
		description = "";
		author = "";
		revision = 0;
		difficulty = 0;
	}
	
	public JsonLevel(int numElements) {
		
		name = "";
		description = "";
		author = "";
		revision = 0;
		difficulty = 0;
		elements = new JsonElement[numElements];
	}
}
