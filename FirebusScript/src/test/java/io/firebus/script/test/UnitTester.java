package io.firebus.script.test;

import java.util.Arrays;

import io.firebus.script.scopes.Scope;
import io.firebus.script.units.Block;
import io.firebus.script.units.Call;
import io.firebus.script.units.CallableDefinition;
import io.firebus.script.units.ExecutionUnit;
import io.firebus.script.units.Expression;
import io.firebus.script.units.Reference;
import io.firebus.script.units.Setter;
import io.firebus.script.units.StringLiteral;
import io.firebus.script.units.operators.AddOperator;
import io.firebus.script.values.impl.Print;

public class UnitTester {

	public static void main(String[] args) {
		try {
			Block b = new Block(
				Arrays.asList(new ExecutionUnit[] {
					new Setter("f", new CallableDefinition(
						Arrays.asList(new String[] {"entry"}),
						new Block(Arrays.asList(new ExecutionUnit[] {
							new Call(
								new Reference("print"), 
								Arrays.asList(new Expression[] {
										new Reference("entry")
								})
							)	
						}))
					)),
					new Setter("v", new StringLiteral("Allo")),
					new Setter("t", new AddOperator(new Reference("v"), new StringLiteral(" toi"))),
					new Call(
						new Reference("f"), 
						Arrays.asList(new Expression[] {
							new Reference("t")
						})
					)
				}
			));
			Print p = new Print();
			Scope s = new Scope();
			s.setValue("print", p);
			b.eval(s);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
