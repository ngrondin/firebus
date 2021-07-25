package io.firebus.script.builder;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import io.firebus.script.units.Block;
import io.firebus.script.units.ExecutionUnit;
import io.firebus.script.units.Expression;
import io.firebus.script.units.UnitContext;
import io.firebus.script.units.statements.While;
import io.firebus.script.parser.JavaScriptParser;
import io.firebus.script.parser.JavaScriptParser.*;

public class MasterBuilder {


	
	public static Block buildProgram(ParseTree ctx) {
		ParseTree sub = ctx.getChild(0);
		return buildSourceElements((SourceElementsContext)sub);
	}
	
	public static Block buildSourceElements(SourceElementsContext ctx) {
		List<ExecutionUnit> list = new ArrayList<ExecutionUnit>();
		for(int i = 0; i < ctx.getChildCount(); i++) {
			ParseTree sub = ctx.getChild(i);
			if(sub instanceof SourceElementContext) {
				list.add(buildSourceElement((SourceElementContext)sub));
			}
		}
		return new Block(list, ContextBuilder.buildContext(ctx));
	}
	
	public static ExecutionUnit buildSourceElement(SourceElementContext ctx) {
		ParseTree sub = ctx.getChild(0);
		if(sub instanceof StatementContext) {
			return buildStatement((StatementContext)sub);
		} else {
			return null;
		}
	}
	
	public static List<ExecutionUnit> buildStatementList(StatementListContext ctx) {
		List<ExecutionUnit> list = new ArrayList<ExecutionUnit>();
		for(int i = 0; i < ctx.getChildCount(); i++) {
			ParseTree sub = ctx.getChild(i);
			if(sub instanceof StatementContext) {
				list.add(buildStatement((StatementContext)sub));
			}
		}
		return list;
	}
	
	public static ExecutionUnit buildStatement(StatementContext ctx) {
		ParseTree sub = ctx.getChild(0);
		if(sub instanceof VariableStatementContext) {
			return buildVariableStatement((VariableStatementContext)sub);
		} else if(sub instanceof ExpressionStatementContext) {
			return ExpressionBuilder.buildExpressionStatement((ExpressionStatementContext)sub);
		} else if(sub instanceof FunctionDeclarationContext) {
			return DeclarationBuilder.buildFunctionDeclaration((FunctionDeclarationContext)sub);
		} else if(sub instanceof BlockContext) {
			return buildBlock((BlockContext)sub);
		} else if(sub instanceof IfStatementContext) {
			return null;
		} else if(sub instanceof IterationStatementContext) {
			return buildIterationStatement((IterationStatementContext)sub);
		} else if(sub instanceof ContinueStatementContext) {
			return null;
		} else if(sub instanceof BreakStatementContext) {
			return null;
		} else if(sub instanceof ReturnStatementContext) {
			return FlowBuilder.buildReturnStatement((ReturnStatementContext)sub);
		} else if(sub instanceof SwitchStatementContext) {
			return null;
		} else if(sub instanceof ThrowStatementContext) {
			return null;
		} else if(sub instanceof TryStatementContext) {
			return null;
		} else {
			return null;
		}
	}
	
	public static ExecutionUnit buildVariableStatement(VariableStatementContext ctx) {
		ParseTree sub = ctx.getChild(0);
		if(sub instanceof VariableDeclarationListContext) {
			return DeclarationBuilder.buildVariableDeclarationList((VariableDeclarationListContext)sub);
		} else {
			return null;
		}
	}
	
	public static ExecutionUnit buildBlock(BlockContext ctx) {
		ParseTree sub = ctx.getChild(1);
		if(sub instanceof StatementListContext) {
			List<ExecutionUnit> list = buildStatementList((StatementListContext)sub);
			return new Block(list, ContextBuilder.buildContext(ctx));
		} else {
			return null;
		}
	}
	
	public static ExecutionUnit buildIterationStatement(IterationStatementContext ctx) {
		UnitContext uc = ContextBuilder.buildContext(ctx);
		TerminalNode tn = (TerminalNode)ctx.getChild(0);
		if(tn.getSymbol().getType() == JavaScriptParser.While) {
			List<Expression> exprSeq = ExpressionBuilder.buildExpressionSequence((ExpressionSequenceContext)ctx.getChild(2));
			ExecutionUnit unit = buildStatement((StatementContext)ctx.getChild(4));
			While w = new While(exprSeq.get(0), unit, uc);
			return w;
		} else {
			return null;
		}
	}
}
