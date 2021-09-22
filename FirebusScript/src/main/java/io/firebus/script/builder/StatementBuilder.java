package io.firebus.script.builder;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import io.firebus.script.SourceInfo;
import io.firebus.script.exceptions.ScriptBuildException;
import io.firebus.script.parser.JavaScriptParser;
import io.firebus.script.parser.JavaScriptParser.BlockContext;
import io.firebus.script.parser.JavaScriptParser.BreakStatementContext;
import io.firebus.script.parser.JavaScriptParser.CaseClauseContext;
import io.firebus.script.parser.JavaScriptParser.CaseClausesContext;
import io.firebus.script.parser.JavaScriptParser.CatchProductionContext;
import io.firebus.script.parser.JavaScriptParser.ContinueStatementContext;
import io.firebus.script.parser.JavaScriptParser.ExpressionSequenceContext;
import io.firebus.script.parser.JavaScriptParser.ExpressionStatementContext;
import io.firebus.script.parser.JavaScriptParser.FinallyProductionContext;
import io.firebus.script.parser.JavaScriptParser.FunctionDeclarationContext;
import io.firebus.script.parser.JavaScriptParser.IfStatementContext;
import io.firebus.script.parser.JavaScriptParser.IterationStatementContext;
import io.firebus.script.parser.JavaScriptParser.ReturnStatementContext;
import io.firebus.script.parser.JavaScriptParser.StatementContext;
import io.firebus.script.parser.JavaScriptParser.StatementListContext;
import io.firebus.script.parser.JavaScriptParser.SwitchStatementContext;
import io.firebus.script.parser.JavaScriptParser.ThrowStatementContext;
import io.firebus.script.parser.JavaScriptParser.TryStatementContext;
import io.firebus.script.parser.JavaScriptParser.VariableDeclarationListContext;
import io.firebus.script.parser.JavaScriptParser.VariableStatementContext;
import io.firebus.script.units.abs.ExecutionUnit;
import io.firebus.script.units.abs.Expression;
import io.firebus.script.units.abs.Statement;
import io.firebus.script.units.expressions.ExpressionStatement;
import io.firebus.script.units.operators.abs.Operator;
import io.firebus.script.units.setters.DeclareList;
import io.firebus.script.units.statements.ArrayLoop;
import io.firebus.script.units.statements.Block;
import io.firebus.script.units.statements.Break;
import io.firebus.script.units.statements.CaseClause;
import io.firebus.script.units.statements.Catch;
import io.firebus.script.units.statements.Do;
import io.firebus.script.units.statements.Finally;
import io.firebus.script.units.statements.ForLoop;
import io.firebus.script.units.statements.If;
import io.firebus.script.units.statements.KeyLoop;
import io.firebus.script.units.statements.Switch;
import io.firebus.script.units.statements.Throw;
import io.firebus.script.units.statements.Try;
import io.firebus.script.units.statements.While;

public class StatementBuilder extends Builder {
	public static List<Statement> buildStatementList(StatementListContext ctx) throws ScriptBuildException {
		List<Statement> list = new ArrayList<Statement>();
		for(int i = 0; i < ctx.getChildCount(); i++) {
			ParseTree sub = ctx.getChild(i);
			if(sub instanceof StatementContext) {
				list.add(buildStatement((StatementContext)sub));
			}
		}
		return list;
	}
	
	public static Statement buildStatement(StatementContext ctx) throws ScriptBuildException {
		ParseTree sub = ctx.getChild(0);
		if(sub instanceof VariableStatementContext) {
			return buildVariableStatement((VariableStatementContext)sub);
		} else if(sub instanceof ExpressionStatementContext) {
			return buildExpressionStatement((ExpressionStatementContext)sub);
		} else if(sub instanceof FunctionDeclarationContext) {
			return DeclarationBuilder.buildFunctionDeclaration((FunctionDeclarationContext)sub);
		} else if(sub instanceof BlockContext) {
			return buildBlock((BlockContext)sub);
		} else if(sub instanceof IfStatementContext) {
			return buildIfStatement((IfStatementContext)sub);
		} else if(sub instanceof IterationStatementContext) {
			return buildIterationStatement((IterationStatementContext)sub);
		} else if(sub instanceof ContinueStatementContext) {

		} else if(sub instanceof BreakStatementContext) {
			return new Break(sourceInfo(ctx));
		} else if(sub instanceof ReturnStatementContext) {
			return FlowBuilder.buildReturnStatement((ReturnStatementContext)sub);
		} else if(sub instanceof SwitchStatementContext) {
			return buildSwitchStatement((SwitchStatementContext)sub);
		} else if(sub instanceof ThrowStatementContext) {
			return buildThrowStatement((ThrowStatementContext)sub);
		} else if(sub instanceof TryStatementContext) {
			return buildTryStatement((TryStatementContext)sub);
		} else {

		}
		throw new ScriptBuildException("Unknown source element", sourceInfo(ctx));
	}
	
	public static Statement buildExpressionStatement(ExpressionStatementContext ctx) throws ScriptBuildException {
		ParseTree sub = ctx.getChild(0);
		if(sub instanceof ExpressionSequenceContext) {
			List<Expression> list = ExpressionBuilder.buildExpressionSequence((ExpressionSequenceContext)sub);
			if(list.size() > 0) 
				return new ExpressionStatement(list.get(0), sourceInfo(ctx));
			else
				throw new ScriptBuildException("Empty expression sequence", sourceInfo(ctx));
		} 
		throw new ScriptBuildException("Unknown source element", sourceInfo(ctx));
	}
	
	public static Statement buildVariableStatement(VariableStatementContext ctx) throws ScriptBuildException {
		ParseTree sub = ctx.getChild(0);
		if(sub instanceof VariableDeclarationListContext) {
			return DeclarationBuilder.buildVariableDeclarationList((VariableDeclarationListContext)sub);
		} 
		throw new ScriptBuildException("Unknown source element", sourceInfo(ctx));
	}
	
	public static Block buildBlock(BlockContext ctx) throws ScriptBuildException {
		List<Statement> list = null;
		if(ctx.getChildCount() == 2) {
			list = new ArrayList<Statement>();
		} else if(ctx.getChildCount() == 3 && ctx.getChild(1) instanceof StatementListContext) {
			list = buildStatementList((StatementListContext)(ctx.getChild(1)));
			
		} 
		return new Block(list, sourceInfo(ctx));
	}
	
	public static Statement buildIterationStatement(IterationStatementContext ctx)  throws ScriptBuildException {
		SourceInfo uc = sourceInfo(ctx);
		TerminalNode tn = (TerminalNode)ctx.getChild(0);
		if(tn.getSymbol().getType() == JavaScriptParser.While) {
			List<Expression> exprSeq = ExpressionBuilder.buildExpressionSequence((ExpressionSequenceContext)ctx.getChild(2));
			ExecutionUnit unit = buildStatement((StatementContext)ctx.getChild(4));
			While loop = new While(exprSeq.get(0), unit, uc);
			return loop;
		} else if(tn.getSymbol().getType() == JavaScriptParser.Do) {
				ExecutionUnit unit = buildStatement((StatementContext)ctx.getChild(1));
				List<Expression> exprSeq = ExpressionBuilder.buildExpressionSequence((ExpressionSequenceContext)ctx.getChild(4));
				Do loop = new Do(unit, exprSeq.get(0), uc);
				return loop;
		} else if(tn.getSymbol().getType() == JavaScriptParser.For) {
			if(ctx.getChild(3).getText().equals(";")) {
				List<Expression> condES = ExpressionBuilder.buildExpressionSequence((ExpressionSequenceContext)ctx.getChild(4));
				List<Expression> opES = ExpressionBuilder.buildExpressionSequence((ExpressionSequenceContext)ctx.getChild(6));
				ExecutionUnit unit = buildStatement((StatementContext)ctx.getChild(8));
				ForLoop loop = null;
				if(ctx.getChild(2) instanceof VariableDeclarationListContext) {
					DeclareList declareList = DeclarationBuilder.buildVariableDeclarationList((VariableDeclarationListContext)ctx.getChild(2));
					loop = new ForLoop(declareList, condES.get(0), (Operator)opES.get(0), unit, uc);
				} else if(ctx.getChild(2) instanceof ExpressionSequenceContext) {
					List<Expression> initialES = ExpressionBuilder.buildExpressionSequence((ExpressionSequenceContext)ctx.getChild(2));
					loop = new ForLoop(initialES.get(0), condES.get(0), (Operator)opES.get(0), unit, uc);
				}
				return loop;				
			} else if(ctx.getChild(3).getText().equals("of")) {
				DeclareList declareList = DeclarationBuilder.buildVariableDeclarationList((VariableDeclarationListContext)ctx.getChild(2));
				List<Expression> arrayES = ExpressionBuilder.buildExpressionSequence((ExpressionSequenceContext)ctx.getChild(4));
				ExecutionUnit unit = buildStatement((StatementContext)ctx.getChild(6));
				ArrayLoop loop = new ArrayLoop(declareList, arrayES.get(0), unit, uc);
				return loop;					
			} else if(ctx.getChild(3).getText().equals("in")) {
				DeclareList declareList = DeclarationBuilder.buildVariableDeclarationList((VariableDeclarationListContext)ctx.getChild(2));
				List<Expression> objectES = ExpressionBuilder.buildExpressionSequence((ExpressionSequenceContext)ctx.getChild(4));
				ExecutionUnit unit = buildStatement((StatementContext)ctx.getChild(6));
				KeyLoop loop = new KeyLoop(declareList, objectES.get(0), unit, uc);
				return loop;					
			}
		}
		throw new ScriptBuildException("Unknown source element", sourceInfo(ctx));
	}
	
	public static Statement buildIfStatement(IfStatementContext ctx) throws ScriptBuildException {
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
		} 
		throw new ScriptBuildException("Unknown source element", sourceInfo(ctx));
	}
	
	public static Statement buildThrowStatement(ThrowStatementContext ctx) throws ScriptBuildException {
		SourceInfo uc = sourceInfo(ctx);
		return new Throw(ExpressionBuilder.buildExpressionSequence((ExpressionSequenceContext)ctx.getChild(1)).get(0), uc);
	}	
	
	public static Statement buildTryStatement(TryStatementContext ctx) throws ScriptBuildException {
		SourceInfo uc = sourceInfo(ctx);
		Block block = buildBlock((BlockContext)ctx.getChild(1));
		Catch cp = null;
		Finally fp = null;
		if(ctx.getChildCount() >= 3) {
			if(ctx.getChild(2) instanceof CatchProductionContext) {
				CatchProductionContext cpc = (CatchProductionContext)ctx.getChild(2);
				cp = new Catch(cpc.getChild(2).getText(), buildBlock((BlockContext)cpc.getChild(4)), sourceInfo(cpc));
			}
			if(ctx.getChildCount() >= 4) {
				if(ctx.getChild(3) instanceof FinallyProductionContext) {
					FinallyProductionContext fpc = (FinallyProductionContext)ctx.getChild(3);
					fp = new Finally(buildBlock((BlockContext)fpc.getChild(1)), sourceInfo(fpc));
				}
			}
		}
		return new Try(block, cp, fp, uc);
	}	
	
	public static Statement buildSwitchStatement(SwitchStatementContext ctx) throws ScriptBuildException {
		SourceInfo uc = sourceInfo(ctx);
		List<Expression> expressions = ExpressionBuilder.buildExpressionSequence((ExpressionSequenceContext)ctx.getChild(2));
		if(expressions.size() > 0) {
			List<CaseClause> ccl = buildCaseClauses((CaseClausesContext)(ctx.getChild(4).getChild(1)));
			Switch s = new Switch(expressions.get(0), ccl, uc);
			return s;
		} else {
			throw new ScriptBuildException("More than 1 expression in a switch", uc);
		}
	}
	
	public static List<CaseClause> buildCaseClauses(CaseClausesContext ctx) throws ScriptBuildException {
		List<CaseClause> list = new ArrayList<CaseClause>();
		for(int i = 0; i < ctx.getChildCount(); i++) {
			CaseClauseContext ccc = (CaseClauseContext)ctx.getChild(i);
			List<Expression> expressions = ExpressionBuilder.buildExpressionSequence((ExpressionSequenceContext)ccc.getChild(1));
			if(expressions.size() > 0) {
				List<Statement> statementList = buildStatementList((StatementListContext)ccc.getChild(3));
				CaseClause cc = new CaseClause(expressions.get(0), statementList, sourceInfo(ccc));
				list.add(cc);
			}
		}
		return list;
	}
}
