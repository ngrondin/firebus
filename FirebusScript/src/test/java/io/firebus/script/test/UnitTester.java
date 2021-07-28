package io.firebus.script.test;

import java.util.Arrays;

import io.firebus.script.scopes.Scope;
import io.firebus.script.units.Block;
import io.firebus.script.units.Call;
import io.firebus.script.units.CallableDefinition;
import io.firebus.script.units.ExecutionUnit;
import io.firebus.script.units.Expression;
import io.firebus.script.units.Setter;
import io.firebus.script.units.literals.StringLiteral;
import io.firebus.script.units.operators.Add;
import io.firebus.script.units.references.Reference;
import io.firebus.script.units.references.VariableReference;
import io.firebus.script.values.impl.Print;

public class UnitTester {

	public static void main(String[] args) {
		try {/*
			Block b = new Block(
				Arrays.asList(new ExecutionUnit[] {
					new VariableSetter("f", new CallableDefinition(
						Arrays.asList(new String[] {"entry"}),
						new Block(Arrays.asList(new ExecutionUnit[] {
							new Call(
								new Reference("print", null), 
								Arrays.asList(new Expression[] {
										new VariableReference("entry", null)
								}),
								null
							)	
						}), null),
						null
					), null),
					new VariableSetter("v", new StringLiteral("Allo", null), null),
					new VariableSetter("t", new Add(new Reference("v", null), new StringLiteral(" toi", null), null), null),
					new Call(
						new Reference("f", null), 
						Arrays.asList(new Expression[] {
							new Reference("t", null)
						}),
						null
					)
				}
			), null);
			Print p = new Print();
			Scope s = new Scope();
			s.setValue("print", p);
			b.eval(s);*/
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
