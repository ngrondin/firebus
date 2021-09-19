package io.firebus.script.test;

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
