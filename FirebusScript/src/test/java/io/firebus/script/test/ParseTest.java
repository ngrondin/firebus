package io.firebus.script.test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import io.firebus.script.Engine;


public class ParseTest {

	public static void main(String[] args) {
		try {
			String source = Files.readString(Paths.get("test.js"), StandardCharsets.US_ASCII);
			Engine engine = new Engine();
			engine.eval(source);
			System.out.println("Done");
		} catch(Exception e) {
			e.printStackTrace();
		}		
	}
}
