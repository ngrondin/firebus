package io.firebus.script.builder;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import io.firebus.script.parser.JavaScriptParser.*;
import io.firebus.script.parser.JavaScriptParserListener;

public class JavaScriptUnitBuilder implements JavaScriptParserListener {

	@Override
	public void visitTerminal(TerminalNode node) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visitErrorNode(ErrorNode node) {
		// TODO Auto-generated method stub
		
	}

	public void enterEveryRule(ParserRuleContext ctx) {
		if(ctx.getChildCount() == 0) { 
			System.out.println("No Children");
		}
	}

	public void exitEveryRule(ParserRuleContext ctx) {
	}

	@Override
	public void enterProgram(ProgramContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitProgram(ProgramContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterSourceElement(SourceElementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitSourceElement(SourceElementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterStatement(StatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitStatement(StatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterBlock(BlockContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitBlock(BlockContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterStatementList(StatementListContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitStatementList(StatementListContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterImportStatement(ImportStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitImportStatement(ImportStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterImportFromBlock(ImportFromBlockContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitImportFromBlock(ImportFromBlockContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterModuleItems(ModuleItemsContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitModuleItems(ModuleItemsContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterImportDefault(ImportDefaultContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitImportDefault(ImportDefaultContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterImportNamespace(ImportNamespaceContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitImportNamespace(ImportNamespaceContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterImportFrom(ImportFromContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitImportFrom(ImportFromContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterAliasName(AliasNameContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitAliasName(AliasNameContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterExportDeclaration(ExportDeclarationContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitExportDeclaration(ExportDeclarationContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterExportDefaultDeclaration(ExportDefaultDeclarationContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitExportDefaultDeclaration(ExportDefaultDeclarationContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterExportFromBlock(ExportFromBlockContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitExportFromBlock(ExportFromBlockContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterDeclaration(DeclarationContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitDeclaration(DeclarationContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterVariableStatement(VariableStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitVariableStatement(VariableStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterVariableDeclarationList(VariableDeclarationListContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitVariableDeclarationList(VariableDeclarationListContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterVariableDeclaration(VariableDeclarationContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitVariableDeclaration(VariableDeclarationContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterEmptyStatement(EmptyStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitEmptyStatement(EmptyStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterExpressionStatement(ExpressionStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitExpressionStatement(ExpressionStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterIfStatement(IfStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitIfStatement(IfStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterDoStatement(DoStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitDoStatement(DoStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterWhileStatement(WhileStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitWhileStatement(WhileStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterForStatement(ForStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitForStatement(ForStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterForInStatement(ForInStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitForInStatement(ForInStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterForOfStatement(ForOfStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitForOfStatement(ForOfStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterVarModifier(VarModifierContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitVarModifier(VarModifierContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterContinueStatement(ContinueStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitContinueStatement(ContinueStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterBreakStatement(BreakStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitBreakStatement(BreakStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterReturnStatement(ReturnStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitReturnStatement(ReturnStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterYieldStatement(YieldStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitYieldStatement(YieldStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterWithStatement(WithStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitWithStatement(WithStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterSwitchStatement(SwitchStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitSwitchStatement(SwitchStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterCaseBlock(CaseBlockContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitCaseBlock(CaseBlockContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterCaseClauses(CaseClausesContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitCaseClauses(CaseClausesContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterCaseClause(CaseClauseContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitCaseClause(CaseClauseContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterDefaultClause(DefaultClauseContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitDefaultClause(DefaultClauseContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterLabelledStatement(LabelledStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitLabelledStatement(LabelledStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterThrowStatement(ThrowStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitThrowStatement(ThrowStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterTryStatement(TryStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitTryStatement(TryStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterCatchProduction(CatchProductionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitCatchProduction(CatchProductionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterFinallyProduction(FinallyProductionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitFinallyProduction(FinallyProductionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterDebuggerStatement(DebuggerStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitDebuggerStatement(DebuggerStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterFunctionDeclaration(FunctionDeclarationContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitFunctionDeclaration(FunctionDeclarationContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterClassDeclaration(ClassDeclarationContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitClassDeclaration(ClassDeclarationContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterClassTail(ClassTailContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitClassTail(ClassTailContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterClassElement(ClassElementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitClassElement(ClassElementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterMethodDefinition(MethodDefinitionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitMethodDefinition(MethodDefinitionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterFormalParameterList(FormalParameterListContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitFormalParameterList(FormalParameterListContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterFormalParameterArg(FormalParameterArgContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitFormalParameterArg(FormalParameterArgContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterLastFormalParameterArg(LastFormalParameterArgContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitLastFormalParameterArg(LastFormalParameterArgContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterFunctionBody(FunctionBodyContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitFunctionBody(FunctionBodyContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterSourceElements(SourceElementsContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitSourceElements(SourceElementsContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterArrayLiteral(ArrayLiteralContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitArrayLiteral(ArrayLiteralContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterElementList(ElementListContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitElementList(ElementListContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterArrayElement(ArrayElementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitArrayElement(ArrayElementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterPropertyExpressionAssignment(PropertyExpressionAssignmentContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitPropertyExpressionAssignment(PropertyExpressionAssignmentContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterComputedPropertyExpressionAssignment(ComputedPropertyExpressionAssignmentContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitComputedPropertyExpressionAssignment(ComputedPropertyExpressionAssignmentContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterFunctionProperty(FunctionPropertyContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitFunctionProperty(FunctionPropertyContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterPropertyGetter(PropertyGetterContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitPropertyGetter(PropertyGetterContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterPropertySetter(PropertySetterContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitPropertySetter(PropertySetterContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterPropertyShorthand(PropertyShorthandContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitPropertyShorthand(PropertyShorthandContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterPropertyName(PropertyNameContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitPropertyName(PropertyNameContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterArguments(ArgumentsContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitArguments(ArgumentsContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterArgument(ArgumentContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitArgument(ArgumentContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterExpressionSequence(ExpressionSequenceContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitExpressionSequence(ExpressionSequenceContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterTemplateStringExpression(TemplateStringExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitTemplateStringExpression(TemplateStringExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterTernaryExpression(TernaryExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitTernaryExpression(TernaryExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterLogicalAndExpression(LogicalAndExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitLogicalAndExpression(LogicalAndExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterPowerExpression(PowerExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitPowerExpression(PowerExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterPreIncrementExpression(PreIncrementExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitPreIncrementExpression(PreIncrementExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterObjectLiteralExpression(ObjectLiteralExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitObjectLiteralExpression(ObjectLiteralExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterMetaExpression(MetaExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitMetaExpression(MetaExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterInExpression(InExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitInExpression(InExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterLogicalOrExpression(LogicalOrExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitLogicalOrExpression(LogicalOrExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterNotExpression(NotExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitNotExpression(NotExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterPreDecreaseExpression(PreDecreaseExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitPreDecreaseExpression(PreDecreaseExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterArgumentsExpression(ArgumentsExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitArgumentsExpression(ArgumentsExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterAwaitExpression(AwaitExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitAwaitExpression(AwaitExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterThisExpression(ThisExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitThisExpression(ThisExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterFunctionExpression(FunctionExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitFunctionExpression(FunctionExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterUnaryMinusExpression(UnaryMinusExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitUnaryMinusExpression(UnaryMinusExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterAssignmentExpression(AssignmentExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitAssignmentExpression(AssignmentExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterPostDecreaseExpression(PostDecreaseExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitPostDecreaseExpression(PostDecreaseExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterTypeofExpression(TypeofExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitTypeofExpression(TypeofExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterInstanceofExpression(InstanceofExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitInstanceofExpression(InstanceofExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterUnaryPlusExpression(UnaryPlusExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitUnaryPlusExpression(UnaryPlusExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterDeleteExpression(DeleteExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitDeleteExpression(DeleteExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterImportExpression(ImportExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitImportExpression(ImportExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterEqualityExpression(EqualityExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitEqualityExpression(EqualityExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterBitXOrExpression(BitXOrExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitBitXOrExpression(BitXOrExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterSuperExpression(SuperExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitSuperExpression(SuperExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterMultiplicativeExpression(MultiplicativeExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitMultiplicativeExpression(MultiplicativeExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterBitShiftExpression(BitShiftExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitBitShiftExpression(BitShiftExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterParenthesizedExpression(ParenthesizedExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitParenthesizedExpression(ParenthesizedExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterAdditiveExpression(AdditiveExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitAdditiveExpression(AdditiveExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterRelationalExpression(RelationalExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitRelationalExpression(RelationalExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterPostIncrementExpression(PostIncrementExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitPostIncrementExpression(PostIncrementExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterYieldExpression(YieldExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitYieldExpression(YieldExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterBitNotExpression(BitNotExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitBitNotExpression(BitNotExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterNewExpression(NewExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitNewExpression(NewExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterLiteralExpression(LiteralExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitLiteralExpression(LiteralExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterArrayLiteralExpression(ArrayLiteralExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitArrayLiteralExpression(ArrayLiteralExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterMemberDotExpression(MemberDotExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitMemberDotExpression(MemberDotExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterClassExpression(ClassExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitClassExpression(ClassExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterMemberIndexExpression(MemberIndexExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitMemberIndexExpression(MemberIndexExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterIdentifierExpression(IdentifierExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitIdentifierExpression(IdentifierExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterBitAndExpression(BitAndExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitBitAndExpression(BitAndExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterBitOrExpression(BitOrExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitBitOrExpression(BitOrExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterAssignmentOperatorExpression(AssignmentOperatorExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitAssignmentOperatorExpression(AssignmentOperatorExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterVoidExpression(VoidExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitVoidExpression(VoidExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterCoalesceExpression(CoalesceExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitCoalesceExpression(CoalesceExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterAssignable(AssignableContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitAssignable(AssignableContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterObjectLiteral(ObjectLiteralContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitObjectLiteral(ObjectLiteralContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterFunctionDecl(FunctionDeclContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitFunctionDecl(FunctionDeclContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterAnoymousFunctionDecl(AnoymousFunctionDeclContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitAnoymousFunctionDecl(AnoymousFunctionDeclContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterArrowFunction(ArrowFunctionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitArrowFunction(ArrowFunctionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterArrowFunctionParameters(ArrowFunctionParametersContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitArrowFunctionParameters(ArrowFunctionParametersContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterArrowFunctionBody(ArrowFunctionBodyContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitArrowFunctionBody(ArrowFunctionBodyContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterAssignmentOperator(AssignmentOperatorContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitAssignmentOperator(AssignmentOperatorContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterLiteral(LiteralContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitLiteral(LiteralContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterNumericLiteral(NumericLiteralContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitNumericLiteral(NumericLiteralContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterBigintLiteral(BigintLiteralContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitBigintLiteral(BigintLiteralContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterGetter(GetterContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitGetter(GetterContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterSetter(SetterContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitSetter(SetterContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterIdentifierName(IdentifierNameContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitIdentifierName(IdentifierNameContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterIdentifier(IdentifierContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitIdentifier(IdentifierContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterReservedWord(ReservedWordContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitReservedWord(ReservedWordContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterKeyword(KeywordContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitKeyword(KeywordContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterLet_(Let_Context ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitLet_(Let_Context ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterEos(EosContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitEos(EosContext ctx) {
		// TODO Auto-generated method stub
		
	}

}
