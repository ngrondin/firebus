package io.firebus.data;

import java.time.ZonedDateTime;

import io.firebus.data.parse.DateParser;

public class TestDate {
	
	public static String[] valids = {
			"2023-05-27T01:35:36+10",
			"2023-05-27T01:35:36+10:00",
			"2023-05-27T01:35:36Z",
			"2023-05-27T01:35:36",
			"2023-05-27T01:35:36.199+10",
			"2023-05-27T01:35:36.199+10:00",
			"2023-05-27T01:35:36.199Z",
			"2023-05-27T01:35:36.199",
			"2023-05-27T01:35:36.199123+10",
			"2023-05-27T01:35:36.199123+10:00",
			"2023-05-27T01:35:36.199123Z",
			"2023-05-27T01:35:36.199123",
			"2023-05-27T01:35:36.1991234-10",
			"2023-05-27T01:35:36.1991234-10:00",
			"2023-05-27T01:35:36.1991234Z",
			"2023-05-27T01:35:36.1991234",
	};
			

	public static void main(String[] args) {
		for(int i = 0; i < valids.length; i++) {
			ZonedDateTime d = DateParser.parse(valids[i]);
			if(d == null) {
				System.err.println(valids[i]);
				System.exit(-1);
			} else {
				System.out.println(d.toString());
			}	
		}
	}
}
