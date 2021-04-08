package io.firebus.script.objects.impl;

import java.util.List;

import io.firebus.script.objects.Callable;
import io.firebus.script.objects.ScriptObject;

public class Print extends Callable {

	public ScriptObject call(List<ScriptObject> params) {
		ScriptObject obj = params.size() > 0 ? params.get(0) : null;
		System.out.println(obj.toString());
		return null;
	}

}
