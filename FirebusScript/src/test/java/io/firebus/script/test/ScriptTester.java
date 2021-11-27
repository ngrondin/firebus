package io.firebus.script.test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.script.Function;
import io.firebus.script.ScriptFactory;
import io.firebus.script.Source;

public class ScriptTester {
	
	public static void main(String[] args) {
		try {
		    Path path = Paths.get("test.js");
		    Stream<String> lines = Files.lines(path);
		    String script = lines.collect(Collectors.joining("\n"));
		    lines.close();
			Source source = new Source("test", script);
			ScriptFactory factory = new ScriptFactory();
			Function function = factory.createFunction(new String[] {"obj", "arr"}, source);
			DataMap map = new DataMap();
			map.put("ms", 1599877452022L);
			DataList list = new DataList();
			list.add("allo");
			list.add("toi");
			Object ret = function.call(map, list);
			if(ret != null)
				System.out.println(ret);
			/*Expression expression = factory.createExpression("0.8 * ((a * b) - 5)");
			Map<String, Object> context = new HashMap<String, Object>();
			context.put("a", 3);
			context.put("b", 7);
			Object res = expression.eval(context);
			System.out.println(res);*/
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
