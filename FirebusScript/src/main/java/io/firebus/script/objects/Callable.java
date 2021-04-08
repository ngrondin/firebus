package io.firebus.script.objects;

import java.util.List;

public abstract class Callable extends ScriptObject {

	public abstract ScriptObject call(List<ScriptObject> params);
}
