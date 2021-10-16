package io.firebus.script.builder;


import java.text.NumberFormat;
import java.text.ParseException;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptBuildException;
import io.firebus.script.parser.JavaScriptParser;
import io.firebus.script.parser.JavaScriptParser.ArrayElementContext;
import io.firebus.script.parser.JavaScriptParser.ArrayLiteralContext;
import io.firebus.script.parser.JavaScriptParser.ElementListContext;
import io.firebus.script.parser.JavaScriptParser.LiteralContext;
import io.firebus.script.parser.JavaScriptParser.NumericLiteralContext;
import io.firebus.script.parser.JavaScriptParser.ObjectLiteralContext;
import io.firebus.script.parser.JavaScriptParser.PropertyExpressionAssignmentContext;
import io.firebus.script.parser.JavaScriptParser.PropertyShorthandContext;
import io.firebus.script.parser.JavaScriptParser.SingleExpressionContext;
import io.firebus.script.units.abs.Expression;
import io.firebus.script.units.abs.Literal;
import io.firebus.script.units.literals.ArrayLiteral;
import io.firebus.script.units.literals.BooleanLiteral;
import io.firebus.script.units.literals.NullLiteral;
import io.firebus.script.units.literals.NumericLiteral;
import io.firebus.script.units.literals.ObjectLiteral;
import io.firebus.script.units.literals.StringLiteral;
import io.firebus.script.units.operators.Spread;
import io.firebus.script.units.references.VariableReference;

public class LiteralBuilder extends Builder {
    
    public static Literal buildLiteral(LiteralContext ctx) throws ScriptBuildException {
		Literal ret = null;
		ParseTree child = ctx.getChild(0);
		if(child instanceof NumericLiteralContext) {
			return buildNumericLiteral((NumericLiteralContext)child);
		} else if(child instanceof TerminalNode) {
			SourceInfo uc = sourceInfo(ctx);
			TerminalNode tn = (TerminalNode)child;
			if(tn.getSymbol().getType() == JavaScriptParser.StringLiteral) {
				String str = tn.getText();
				if((str.startsWith("\"") && str.endsWith("\"")) || (str.startsWith("'") && str.endsWith("'"))) {
					boolean singleQuote = str.startsWith("'") ? true : false;
					str = str.substring(1, str.length() - 1);
					str = str
						.replace("\\r", "\r")
						.replace("\\n", "\n")
						.replace("\\t", "\t")
						.replace("\\/", "/");
					if(singleQuote) 
						str = str.replace("\\'", "'");
					else 
						str = str.replace("\\\"", "\"");
				}	
				ret = new StringLiteral(str, uc);
			} else if(tn.getSymbol().getType() == JavaScriptParser.NullLiteral) {
				ret = new NullLiteral(uc);
			} else if(tn.getSymbol().getType() == JavaScriptParser.BooleanLiteral) {
				ret = new BooleanLiteral(tn.getText().equalsIgnoreCase("true") ? true : false, uc);
			}   
		}
		return ret;
	}

    public static NumericLiteral buildNumericLiteral(NumericLiteralContext ctx) throws ScriptBuildException {
    	SourceInfo uc = sourceInfo(ctx);
		TerminalNode tn = (TerminalNode)ctx.getChild(0);
		Number number = null;
		if(tn.getSymbol().getType() == JavaScriptParser.DecimalLiteral) {
			try {
				number = NumberFormat.getInstance().parse(tn.getText());
			} catch(ParseException e) {
				throw new ScriptBuildException("Error parsing number format", e);
			}
		} else if(tn.getSymbol().getType() == JavaScriptParser.HexIntegerLiteral) {
			number = Long.decode(tn.getText());
		} else if(tn.getSymbol().getType() == JavaScriptParser.OctalIntegerLiteral) {
			number = Long.parseLong(tn.getText(), 8);
		} else if(tn.getSymbol().getType() == JavaScriptParser.OctalIntegerLiteral2) {
			number = Long.parseLong(tn.getText().substring(2), 8);
		} else if(tn.getSymbol().getType() == JavaScriptParser.BinaryIntegerLiteral) {
			number = Long.parseLong(tn.getText().substring(2), 2);
		} 
		return new NumericLiteral(number, uc);
    }
 
    public static ObjectLiteral buildObjectLiteral(ObjectLiteralContext ctx) throws ScriptBuildException {
    	ObjectLiteral ol = new ObjectLiteral(sourceInfo(ctx));
    	for(ParseTree sub: ctx.children) {
    		if(sub instanceof PropertyExpressionAssignmentContext) {
    			String key = sub.getChild(0).getText();
    			if((key.startsWith("'") && key.endsWith("'")) || (key.startsWith("\"") && key.endsWith("\""))) 
    				key = key.substring(1, key.length() - 1);
    			Expression val = ExpressionBuilder.buildSingleExpression((SingleExpressionContext)sub.getChild(2));
    			ol.addSetter(key, val);
    		} else if(sub instanceof PropertyShorthandContext) {
    			VariableReference reference = (VariableReference)ExpressionBuilder.buildSingleExpression((SingleExpressionContext)sub.getChild(0));
    			ol.addSetter(reference.getName(), reference);
    		}
    	}
    	return ol;
    }
    
    public static ArrayLiteral buildArrayLiteral(ArrayLiteralContext ctx) throws ScriptBuildException {
    	ArrayLiteral al = new ArrayLiteral(sourceInfo(ctx));
    	ElementListContext el = (ElementListContext)ctx.getChild(1);
    	if(el.getChildCount() > 0) {
	    	for(ParseTree sub: el.children) {
	    		if(sub instanceof ArrayElementContext) {
	    			if(sub.getChildCount() == 1) {
	    				Expression val = ExpressionBuilder.buildSingleExpression((SingleExpressionContext)sub.getChild(0));
		    			al.addExpression(val);	    				
	    			} else if(sub.getChildCount() == 2 && sub.getChild(0).getText().equals("...")) {
	    				Expression val = ExpressionBuilder.buildSingleExpression((SingleExpressionContext)sub.getChild(1));
	    				al.addExpression(new Spread(val, sourceInfo(ctx)));
	    			}
	    		}
	    	}
    	}
    	return al;
    }
}
