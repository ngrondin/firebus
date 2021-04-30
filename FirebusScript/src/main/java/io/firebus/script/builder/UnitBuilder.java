package io.firebus.script.builder;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import io.firebus.script.units.Block;
import io.firebus.script.units.Call;
import io.firebus.script.units.Declare;
import io.firebus.script.units.DeclareList;
import io.firebus.script.units.ExecutionUnit;
import io.firebus.script.units.Expression;
import io.firebus.script.units.Literal;
import io.firebus.script.units.Reference;
import io.firebus.script.units.StringLiteral;
import io.firebus.script.parser.JavaScriptParser.*;

public class UnitBuilder {

	public UnitBuilder() {
		
	}
	
	public Block buildProgram(ParseTree ctx) {
		ParseTree sub = ctx.getChild(0);
		return buildSourceElements(sub);
	}
	
	public Block buildSourceElements(ParseTree ctx) {
		List<ExecutionUnit> list = new ArrayList<ExecutionUnit>();
		for(int i = 0; i < ctx.getChildCount(); i++) {
			ParseTree sub = ctx.getChild(i);
			if(sub instanceof SourceElementContext) {
				list.add(buildSourceElement((SourceElementContext)sub));
			}
		}
		return new Block(list);
	}
	
	public ExecutionUnit buildSourceElement(ParseTree ctx) {
		ParseTree sub = ctx.getChild(0);
		if(sub instanceof StatementContext) {
			return buildStatement(sub);
		} else {
			return null;
		}
	}
	
	public ExecutionUnit buildStatement(ParseTree ctx) {
		ParseTree sub = ctx.getChild(0);
		if(sub instanceof VariableStatementContext) {
			return buildVariableStatement(sub);
		} else if(sub instanceof ExpressionStatementContext) {
			return buildExpressionStatement(sub);
		} else {
			return null;
		}
	}
	
	public ExecutionUnit buildVariableStatement(ParseTree ctx) {
		ParseTree sub = ctx.getChild(0);
		if(sub instanceof VariableDeclarationListContext) {
			return buildVariableDeclarationList(sub);
		} else {
			return null;
		}
	}
	
	public DeclareList buildVariableDeclarationList(ParseTree ctx) {
		List<Declare> list = new ArrayList<Declare>();
		String modifier = null;
		for(int i = 0; i < ctx.getChildCount(); i++) {
			ParseTree sub = ctx.getChild(i);
			if(sub instanceof VarModifierContext) {
				modifier = sub.getText();
			} else if(sub instanceof VariableDeclarationContext) {
				list.add(buildVariableDeclaration(sub));
			}
		}
		return new DeclareList(modifier, list);
	}
	
	public Declare buildVariableDeclaration(ParseTree ctx) {
		String key = buildAssignable(ctx.getChild(0));
		ParseTree sub = ctx.getChild(2);
		Expression exp = buildSingleExpression(sub);
		return new Declare(key, exp);
	}

	public Expression buildExpressionStatement(ParseTree ctx) {
		ParseTree sub = ctx.getChild(0);
		if(sub instanceof ExpressionSequenceContext) {
			return buildExpressionSequence((ExpressionSequenceContext)sub);
		} else {
			return null;
		}
	}
	
	public Expression buildExpressionSequence(ParseTree ctx) {
		ParseTree sub = ctx.getChild(0);
		if(sub instanceof SingleExpressionContext) {
			return buildSingleExpression((SingleExpressionContext)sub);
		} else {
			return null;
		}
	}
	
	public Expression buildSingleExpression(ParseTree ctx) {
		if(ctx.getChildCount() == 1) {
			ParseTree sub = ctx.getChild(0);
			if(sub instanceof LiteralContext) {
				return buildLiteral((LiteralContext)sub);
			} else if(sub instanceof IdentifierContext) {
				return buildIdentifier(sub);
			} else {
				return null;
			}
		} else if(ctx.getChildCount() == 2) {
			ParseTree sub1 = ctx.getChild(0);
			ParseTree sub2 = ctx.getChild(1);
			if(sub1 instanceof SingleExpressionContext && sub2 instanceof ArgumentsContext) {
				return new Call(buildSingleExpression(sub1), buildArguments(sub2));
			} else {
				return null;
			}
		} else if(ctx.getChildCount() == 3) {
			return null;
		} else {
			return null;
		}
	}
	
	public List<Expression> buildArguments(ParseTree ctx) {
		List<Expression> list = new ArrayList<Expression>();
		for(int i = 0; i < ctx.getChildCount(); i++) {
			ParseTree sub = ctx.getChild(i);
			if(sub instanceof ArgumentContext) {
				list.add(buildArgument(sub));
			}
		}
		return list;
	}
	
	public Expression buildArgument(ParseTree ctx) {
		ParseTree sub = ctx.getChild(0);
		if(sub instanceof SingleExpressionContext) {
			return buildSingleExpression(sub);
		} else if(sub instanceof IdentifierContext) {
			return buildIdentifier(sub);
		} else {
			return null;
		}
	}
	
	public String buildAssignable(ParseTree ctx) {
		return ctx.getChild(0).getText(); 
		//TODO: This is not right;
		
	}
	
	public Reference buildIdentifier(ParseTree ctx) {
		return new Reference(ctx.getText());
	}
	
	public Literal buildLiteral(ParseTree ctx) {
		return new StringLiteral(ctx.getText());
	}
}
