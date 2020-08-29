package io.firebus;

import java.io.FileInputStream;

import io.firebus.utils.DataFilter;
import io.firebus.utils.DataMap;

public class FilterTest {
	
	public static void main(String[] args) {
		try {
			DataMap data = new DataMap(new FileInputStream("c:/tmp/data.json"));
			DataMap filterMap = new DataMap(new FileInputStream("c:/tmp/filter.json"));
			DataFilter filter = new DataFilter(filterMap);
			System.out.println(filter.apply(data));			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
