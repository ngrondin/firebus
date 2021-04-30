package io.firebus.script.values;

import java.util.List;

public abstract class SCallable extends SValue {

	public abstract SValue call(List<SValue> params);
}
