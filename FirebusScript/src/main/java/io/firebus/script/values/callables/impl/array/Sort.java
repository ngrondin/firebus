package io.firebus.script.values.callables.impl.array;

import java.util.ArrayList;
import java.util.List;

import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.values.SArray;
import io.firebus.script.values.SNumber;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.ArrayFunction;

public class Sort extends ArrayFunction {
	
	public Sort(List<SValue> v) {
		super(v);
	}
	
	public SValue call(SValue[] arguments) throws ScriptException {
		SCallable c = (SCallable)arguments[0];
		List<SValue> out = new ArrayList<SValue>();
		for(int i = 0; i < values.size(); i++) {
			SValue itema = values.get(i);
			boolean inserted = false;
			for(int j = 0; j < out.size(); j++) {
				SValue itemb = out.get(j);
				SValue comp = c.call(new SValue[] {itema, itemb});
				if(comp instanceof SNumber) {
					int compInt = ((SNumber)comp).getNumber().intValue();
					if(compInt > 0) {
						out.add(j, itema);
						inserted = true;
						break;
					}
				}
			}
			if(!inserted) 
				out.add(itema);
		}
		return new SArray(out);
	}

}
