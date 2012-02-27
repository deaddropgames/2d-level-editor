package ca.squadcar.games.editor;

import java.io.File;

import org.ini4j.Ini;

public class LevelEditorSettings {

	private float startX;
	private float startY;
	private String simExe;
	private String simArgs;
	
	public LevelEditorSettings() {
		
		startX = -5.0f;
		startY = 2.0f;
		simExe = "";
		simArgs = "${level}";
	}
	
	public boolean loadFromFile(String filename) {
		
		try {
			
			Ini ini = new Ini(new File(filename));
			if(ini.isEmpty()) {
				
				System.err.println(String.format("Input INI file '%s' is empty.", filename));
				return false;
			}
			
			this.startX = ini.get("export", "startX", float.class);
			this.startY = ini.get("export", "startY", float.class);
			this.simExe = ini.get("simulator", "simExe", String.class);
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

	public float getStartX() {
		
		return startX;
	}

	public void setStartX(float startX) {
		
		this.startX = startX;
	}

	public float getStartY() {
		
		return startY;
	}

	public void setStartY(float startY) {
		
		this.startY = startY;
	}

	public String getSimExe() {
		
		return simExe;
	}

	public void setSimExe(String simExe) {
		
		this.simExe = simExe;
	}

	public String getSimArgs() {
		
		return simArgs;
	}

	public void setSimArgs(String simArgs) {
		
		this.simArgs = simArgs;
	}
}
