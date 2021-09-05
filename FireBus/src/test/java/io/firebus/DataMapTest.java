package io.firebus;

import java.nio.charset.StandardCharsets;

import io.firebus.data.DataMap;

public class DataMapTest {

	public static void main(String[] args) {
		byte[] bytes = {123, 34, 108, 34, 58, 34, 110, -61, -68, 103, 34, 125};
		String s = new String(bytes, StandardCharsets.UTF_8);
		s = "{\"key\":\"allo \\\"\\n\\r\\t toi \"}";
		System.out.println(s);
		try {
			DataMap map = new DataMap(s);
			System.out.println(map.toString());
		} catch(Exception e) {
			
		}
	}
}
