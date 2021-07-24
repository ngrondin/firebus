package io.firebus.script.builder;

import org.antlr.v4.runtime.tree.ParseTree;

import io.firebus.script.parser.JavaScriptParser.NumericLiteralContext;
import io.firebus.script.units.Literal;
import io.firebus.script.units.literals.NumericLiteral;

public class LiteralBuilder {
    
    public static Literal buildLiteral(ParseTree ctx) {
		Literal ret = null;
		ParseTree child = ctx.getChild(0);
		if(child instanceof NumericLiteralContext) {
			NumericLiteralContext nlc = (NumericLiteralContext)child;

			TerminalNode tn = (TerminalNode)nlc.getChild(0);
			double d = Double.parseDouble(tn.getText());
			ret = new NumericLiteral(d);
		} else  if(child instanceof TerminalNode) {
			TerminalNode tn = (TerminalNode)child;
			String str = tn.getText();
			if(str.startsWith("\"") && str.endsWith("\"")) {
				ret = new StringLiteral(str.substring(1, str.length() - 1));
			}
		}
		return ret;
	}

    public static NumericLiteral buildNumericLiteral(ParseTree ctx) {
        
    }
}
