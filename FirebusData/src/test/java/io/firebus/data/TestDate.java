package io.firebus.data;

import io.firebus.data.parse.DateParser;

public class TestDate {

	public static void main(String[] args) {
		//if(DateParser.parse("2023-01-01T01:34:00.000Z") == null) System.exit(-1);
		System.out.println(DateParser.parse("2023-01-01T01:34:00.00"));
	}
}
