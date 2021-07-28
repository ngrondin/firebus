package io.firebus.script.units.references;

import io.firebus.script.SourceInfo;

public abstract class KeyBasedReference extends Reference {
	protected String key;
	
	public KeyBasedReference(String k, SourceInfo uc) {
		super(uc);
		key = k;
	}
	
	public String getKey() {
		return key;
	}

}
