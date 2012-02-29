package ca.squadcar.games.editor;

import java.io.File;

import org.ini4j.Ini;

public class LevelEditorSettings {

	private String simJar;
	private String simArgs;
	
	public LevelEditorSettings() {
		
		simJar = "";
		simArgs = "${level}";
	}
	
	public boolean loadFromFile(String filename) {
		
		try {
			
			Ini ini = new Ini(new File(filename));
			if(ini.isEmpty()) {
				
				System.err.println(String.format("Input INI file '%s' is empty.", filename));
				return false;
			}
			
			this.simJar = ini.get("simulator", "simJar", String.class);
			this.simArgs = ini.get("simulator", "simArgs", String.class);
		} catch(Exception ex) {
			
			ex.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean saveToFile(String filename) {
		
		// TODO
		return false;
	}

	public String getSimJar() {
		
		return simJar;
	}

	public void setSimJar(String simJar) {
		
		this.simJar = simJar;
	}

	public String getSimArgs() {
		
		return simArgs;
	}

	public void setSimArgs(String simArgs) {
		
		this.simArgs = simArgs;
	}
}
