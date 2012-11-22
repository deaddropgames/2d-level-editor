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
		revision = 1;
		difficulty = 0;
	}
	
	public JsonLevel(int numElements, final JsonLevel level) {
		
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
		
		elements = new JsonElement[numElements];
	}
}
