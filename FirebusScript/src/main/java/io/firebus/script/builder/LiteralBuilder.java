package io.firebus.script.builder;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import io.firebus.script.parser.JavaScriptParser;
import io.firebus.script.parser.JavaScriptParser.LiteralContext;
import io.firebus.script.parser.JavaScriptParser.NumericLiteralContext;
import io.firebus.script.parser.JavaScriptParser.ObjectLiteralContext;
import io.firebus.script.parser.JavaScriptParser.ObjectLiteralExpressionContext;
import io.firebus.script.parser.JavaScriptParser.PropertyAssignmentContext;
import io.firebus.script.parser.JavaScriptParser.SingleExpressionContext;
import io.firebus.script.units.Expression;
import io.firebus.script.units.Literal;
import io.firebus.script.units.PropertySetter;
import io.firebus.script.units.UnitContext;
import io.firebus.script.units.literals.BooleanLiteral;
import io.firebus.script.units.literals.NullLiteral;
import io.firebus.script.units.literals.NumericLiteral;
import io.firebus.script.units.literals.ObjectLiteral;
import io.firebus.script.units.literals.StringLiteral;

public class LiteralBuilder {
    
    public static Literal buildLiteral(LiteralContext ctx) {
		Literal ret = null;
		ParseTree child = ctx.getChild(0);
		if(child instanceof NumericLiteralContext) {
			return buildNumericLiteral((NumericLiteralContext)child);
		} else if(child instanceof TerminalNode) {
			UnitContext uc = new UnitContext(ctx.getStart().getTokenSource().getSourceName(), ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
			TerminalNode tn = (TerminalNode)child;
			if(tn.getSymbol().getType() == JavaScriptParser.StringLiteral) {
				String str = tn.getText();
				if((str.startsWith("\"") && str.endsWith("\"")) || (str.startsWith("'") && str.endsWith("'"))) {
					ret = new StringLiteral(str.substring(1, str.length() - 1), uc);
				}			
			} else if(tn.getSymbol().getType() == JavaScriptParser.NullLiteral) {
				ret = new NullLiteral(uc);
			} else if(tn.getSymbol().getType() == JavaScriptParser.BooleanLiteral) {
				ret = new BooleanLiteral(tn.getText().equalsIgnoreCase("true") ? true : false, uc);
			}   
		}
		return ret;
	}

    public static NumericLiteral buildNumericLiteral(NumericLiteralContext ctx) {
    	UnitContext uc = new UnitContext(ctx.getStart().getTokenSource().getSourceName(), ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
		TerminalNode tn = (TerminalNode)ctx.getChild(0);
		Number number = null;
		if(tn.getSymbol().getType() == JavaScriptParser.DecimalLiteral) {
			double d = Double.parseDouble(tn.getText());
			if(d == (int)d) {
				number = (int)d;
			} else {
				number = d;
			}
		} else if(tn.getSymbol().getType() == JavaScriptParser.HexIntegerLiteral) {
			number = Integer.decode(tn.getText());
		} else if(tn.getSymbol().getType() == JavaScriptParser.OctalIntegerLiteral) {
			number = Integer.parseInt(tn.getText(), 8);
		} else if(tn.getSymbol().getType() == JavaScriptParser.OctalIntegerLiteral2) {
			number = Integer.parseInt(tn.getText().substring(2), 8);
		} else if(tn.getSymbol().getType() == JavaScriptParser.BinaryIntegerLiteral) {
			number = Integer.parseInt(tn.getText().substring(2), 2);
		} 
		return new NumericLiteral(number, uc);
    }
    
    /*public static StringLiteral buildStringLiteral(ParseTree ctx) {
        return null;
    }*/
 
    public static ObjectLiteral buildObjectLiteral(ObjectLiteralContext ctx) {
    	List<PropertySetter> setters = new ArrayList<PropertySetter>();
    	for(ParseTree sub: ctx.children) {
    		if(sub instanceof PropertyAssignmentContext) {
    			PropertyAssignmentContext subCtx = (PropertyAssignmentContext)sub;
    			String key = sub.getChild(0).getText();
    			Expression val = ExpressionBuilder.buildSingleExpression((SingleExpressionContext)sub.getChild(2));
    			setters.add(new PropertySetter(key, val, ContextBuilder.buildContext(subCtx)));
    		}
    	}
    	return new ObjectLiteral(setters, ContextBuilder.buildContext(ctx));
    }
}
