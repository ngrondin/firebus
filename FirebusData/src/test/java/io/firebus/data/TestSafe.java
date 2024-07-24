package io.firebus.data;

public class TestSafe {

	public static void main(String[] args) {
		try {
			DataMap map = new DataMap();
			map.put("n", 1.2);
			map.put("nan", Double.NaN);
			map.put("pi", Double.POSITIVE_INFINITY);
			map.put("ni", Double.NEGATIVE_INFINITY);
			String str = map.toString();
			System.out.println(str);
			DataMap newMap = new DataMap(str);
			System.out.println(newMap);
			System.out.println(map.toString(false, true));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
