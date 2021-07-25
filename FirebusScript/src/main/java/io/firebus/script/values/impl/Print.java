package io.firebus.script.values.impl;

import java.util.List;

import io.firebus.script.values.SCallable;
import io.firebus.script.values.SNull;
import io.firebus.script.values.SValue;

public class Print extends SCallable {

	public SValue call(List<SValue> params) {
		SValue obj = params.size() > 0 ? params.get(0) : null;
		System.out.println(obj.toString());
		return new SNull();
	}

}
