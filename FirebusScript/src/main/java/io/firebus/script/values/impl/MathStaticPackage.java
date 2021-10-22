package io.firebus.script.values.impl;

import io.firebus.script.values.SNumber;
import io.firebus.script.values.SUndefined;
import io.firebus.script.values.abs.SPredefinedObject;
import io.firebus.script.values.abs.SValue;
import io.firebus.script.values.callables.impl.math.Abs;
import io.firebus.script.values.callables.impl.math.Atan2;
import io.firebus.script.values.callables.impl.math.Ceil;
import io.firebus.script.values.callables.impl.math.Cos;
import io.firebus.script.values.callables.impl.math.Floor;
import io.firebus.script.values.callables.impl.math.Max;
import io.firebus.script.values.callables.impl.math.Min;
import io.firebus.script.values.callables.impl.math.Pow;
import io.firebus.script.values.callables.impl.math.Random;
import io.firebus.script.values.callables.impl.math.Round;
import io.firebus.script.values.callables.impl.math.Sin;
import io.firebus.script.values.callables.impl.math.Sqrt;

public class MathStaticPackage extends SPredefinedObject {

	public MathStaticPackage() {
		
	}
	
	public String[] getMemberKeys() {
		return new String[] {"min", "max", "floor", "ceil"};
	}

	public SValue getMember(String name)  {
		if(name.equals("min")) {
			return new Min();
		} else if(name.equals("max")) {
			return new Max();
		} else if(name.equals("floor")) {
			return new Floor();
		} else if(name.equals("ceil")) {
			return new Ceil();
		} else if(name.equals("round")) {
			return new Round();
		} else if(name.equals("abs")) {
			return new Abs();
		} else if(name.equals("random")) {
			return new Random();
		} else if(name.equals("sin")) {
			return new Sin();
		} else if(name.equals("cos")) {
			return new Cos();
		} else if(name.equals("atan2")) {
			return new Atan2();
		} else if(name.equals("sqrt")) {
			return new Sqrt();
		} else if(name.equals("pow")) {
			return new Pow();
		} else if(name.equals("PI")) {
			return new SNumber(Math.PI);
		}
		return SUndefined.get();
	}

}
