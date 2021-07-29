package io.firebus.script.builder;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import io.firebus.script.units.Block;
import io.firebus.script.units.DeclareList;
import io.firebus.script.units.ExecutionUnit;
import io.firebus.script.units.Expression;
import io.firebus.script.units.operators.abs.Operator;
import io.firebus.script.units.statements.ArrayLoop;
import io.firebus.script.units.statements.ForLoop;
import io.firebus.script.units.statements.If;
import io.firebus.script.units.statements.While;
import io.firebus.script.SourceInfo;
import io.firebus.script.parser.JavaScriptParser;
import io.firebus.script.parser.JavaScriptParser.*;

public class MasterBuilder extends Builder {


	
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
		return new Block(list, sourceInfo(ctx));
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
			return buildIfStatement((IfStatementContext)sub);
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
			return new Block(list, sourceInfo(ctx));
		} else {
			return null;
		}
	}
	
	public static ExecutionUnit buildIterationStatement(IterationStatementContext ctx) {
		SourceInfo uc = sourceInfo(ctx);
		TerminalNode tn = (TerminalNode)ctx.getChild(0);
		if(tn.getSymbol().getType() == JavaScriptParser.While) {
			List<Expression> exprSeq = ExpressionBuilder.buildExpressionSequence((ExpressionSequenceContext)ctx.getChild(2));
			ExecutionUnit unit = buildStatement((StatementContext)ctx.getChild(4));
			While loop = new While(exprSeq.get(0), unit, uc);
			return loop;
		} else if(tn.getSymbol().getType() == JavaScriptParser.For) {
			if(ctx.getChild(3).getText().equals(";")) {
				DeclareList declareList = DeclarationBuilder.buildVariableDeclarationList((VariableDeclarationListContext)ctx.getChild(2));
				List<Expression> condES = ExpressionBuilder.buildExpressionSequence((ExpressionSequenceContext)ctx.getChild(4));
				List<Expression> opES = ExpressionBuilder.buildExpressionSequence((ExpressionSequenceContext)ctx.getChild(6));
				ExecutionUnit unit = buildStatement((StatementContext)ctx.getChild(8));
				ForLoop loop = new ForLoop(declareList, condES.get(0), (Operator)opES.get(0), unit, uc);
				return loop;				
			} else if(ctx.getChild(3).getText().equals("of")) {
				DeclareList declareList = DeclarationBuilder.buildVariableDeclarationList((VariableDeclarationListContext)ctx.getChild(2));
				List<Expression> arrayES = ExpressionBuilder.buildExpressionSequence((ExpressionSequenceContext)ctx.getChild(4));
				ExecutionUnit unit = buildStatement((StatementContext)ctx.getChild(6));
				ArrayLoop loop = new ArrayLoop(declareList, arrayES.get(0), unit, uc);
				return loop;					
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	public static ExecutionUnit buildIfStatement(IfStatementContext ctx) {
		SourceInfo uc = sourceInfo(ctx);
		TerminalNode tn = (TerminalNode)ctx.getChild(0);
		if(tn.getSymbol().getType() == JavaScriptParser.If) {
			List<Expression> exprSeq = ExpressionBuilder.buildExpressionSequence((ExpressionSequenceContext)ctx.getChild(2));
			ExecutionUnit unit = buildStatement((StatementContext)ctx.getChild(4));
			ExecutionUnit elseUnit = null;
			if(ctx.getChildCount() >= 7)
				elseUnit = buildStatement((StatementContext)ctx.getChild(6));
			If ret = new If(exprSeq.get(0), unit, elseUnit, uc);
			return ret;
		} else {
			return null;
		}
	}
}
