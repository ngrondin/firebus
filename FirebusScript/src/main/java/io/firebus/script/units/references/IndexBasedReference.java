package io.firebus.script.units.references;

import io.firebus.script.SourceInfo;

public abstract class IndexBasedReference extends Reference {
	protected int index;
	
	public IndexBasedReference(int i, SourceInfo uc) {
		super(uc);
		index = i;
	}
	
	public int getIndex() {
		return index;
	}

}
