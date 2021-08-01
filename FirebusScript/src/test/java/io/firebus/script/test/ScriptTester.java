package io.firebus.script.test;

import java.nio.file.Files;
import java.nio.file.Path;

import io.firebus.script.Engine;

public class ScriptTester {
	
	public static void main(String[] args) {
		try {
		    Path fileName = Path.of("test.js");
			String script = Files.readString(fileName);
			Engine engine = new Engine();
			engine.eval(script);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
