package io.firebus.script.builder;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import io.firebus.script.parser.JavaScriptParser.*;
import io.firebus.script.parser.JavaScriptParserListener;

public class JavaScriptUnitBuilder implements JavaScriptParserListener {

	public void visitTerminal(TerminalNode node) {
		// TODO Auto-generated method stub
		
	}

	
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

	
	public void enterProgram(ProgramContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitProgram(ProgramContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterSourceElement(SourceElementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitSourceElement(SourceElementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterStatement(StatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitStatement(StatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterBlock(BlockContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitBlock(BlockContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterStatementList(StatementListContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitStatementList(StatementListContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterImportStatement(ImportStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitImportStatement(ImportStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterImportFromBlock(ImportFromBlockContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitImportFromBlock(ImportFromBlockContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterModuleItems(ModuleItemsContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitModuleItems(ModuleItemsContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterImportDefault(ImportDefaultContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitImportDefault(ImportDefaultContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterImportNamespace(ImportNamespaceContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitImportNamespace(ImportNamespaceContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterImportFrom(ImportFromContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitImportFrom(ImportFromContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterAliasName(AliasNameContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitAliasName(AliasNameContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterExportDeclaration(ExportDeclarationContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitExportDeclaration(ExportDeclarationContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterExportDefaultDeclaration(ExportDefaultDeclarationContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitExportDefaultDeclaration(ExportDefaultDeclarationContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterExportFromBlock(ExportFromBlockContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitExportFromBlock(ExportFromBlockContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterDeclaration(DeclarationContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitDeclaration(DeclarationContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterVariableStatement(VariableStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitVariableStatement(VariableStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterVariableDeclarationList(VariableDeclarationListContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitVariableDeclarationList(VariableDeclarationListContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterVariableDeclaration(VariableDeclarationContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitVariableDeclaration(VariableDeclarationContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterEmptyStatement(EmptyStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitEmptyStatement(EmptyStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterExpressionStatement(ExpressionStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitExpressionStatement(ExpressionStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterIfStatement(IfStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitIfStatement(IfStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterDoStatement(DoStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitDoStatement(DoStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterWhileStatement(WhileStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitWhileStatement(WhileStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterForStatement(ForStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitForStatement(ForStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterForInStatement(ForInStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitForInStatement(ForInStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterForOfStatement(ForOfStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitForOfStatement(ForOfStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterVarModifier(VarModifierContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitVarModifier(VarModifierContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterContinueStatement(ContinueStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitContinueStatement(ContinueStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterBreakStatement(BreakStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitBreakStatement(BreakStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterReturnStatement(ReturnStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitReturnStatement(ReturnStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterYieldStatement(YieldStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitYieldStatement(YieldStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterWithStatement(WithStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitWithStatement(WithStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterSwitchStatement(SwitchStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitSwitchStatement(SwitchStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterCaseBlock(CaseBlockContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitCaseBlock(CaseBlockContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterCaseClauses(CaseClausesContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitCaseClauses(CaseClausesContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterCaseClause(CaseClauseContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitCaseClause(CaseClauseContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterDefaultClause(DefaultClauseContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitDefaultClause(DefaultClauseContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterLabelledStatement(LabelledStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitLabelledStatement(LabelledStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterThrowStatement(ThrowStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitThrowStatement(ThrowStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterTryStatement(TryStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitTryStatement(TryStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterCatchProduction(CatchProductionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitCatchProduction(CatchProductionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterFinallyProduction(FinallyProductionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitFinallyProduction(FinallyProductionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterDebuggerStatement(DebuggerStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitDebuggerStatement(DebuggerStatementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterFunctionDeclaration(FunctionDeclarationContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitFunctionDeclaration(FunctionDeclarationContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterClassDeclaration(ClassDeclarationContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitClassDeclaration(ClassDeclarationContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterClassTail(ClassTailContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitClassTail(ClassTailContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterClassElement(ClassElementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitClassElement(ClassElementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterMethodDefinition(MethodDefinitionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitMethodDefinition(MethodDefinitionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterFormalParameterList(FormalParameterListContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitFormalParameterList(FormalParameterListContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterFormalParameterArg(FormalParameterArgContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitFormalParameterArg(FormalParameterArgContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterLastFormalParameterArg(LastFormalParameterArgContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitLastFormalParameterArg(LastFormalParameterArgContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterFunctionBody(FunctionBodyContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitFunctionBody(FunctionBodyContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterSourceElements(SourceElementsContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitSourceElements(SourceElementsContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterArrayLiteral(ArrayLiteralContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitArrayLiteral(ArrayLiteralContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterElementList(ElementListContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitElementList(ElementListContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterArrayElement(ArrayElementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitArrayElement(ArrayElementContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterPropertyExpressionAssignment(PropertyExpressionAssignmentContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitPropertyExpressionAssignment(PropertyExpressionAssignmentContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterComputedPropertyExpressionAssignment(ComputedPropertyExpressionAssignmentContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitComputedPropertyExpressionAssignment(ComputedPropertyExpressionAssignmentContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterFunctionProperty(FunctionPropertyContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitFunctionProperty(FunctionPropertyContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterPropertyGetter(PropertyGetterContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitPropertyGetter(PropertyGetterContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterPropertySetter(PropertySetterContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitPropertySetter(PropertySetterContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterPropertyShorthand(PropertyShorthandContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitPropertyShorthand(PropertyShorthandContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterPropertyName(PropertyNameContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitPropertyName(PropertyNameContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterArguments(ArgumentsContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitArguments(ArgumentsContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterArgument(ArgumentContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitArgument(ArgumentContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterExpressionSequence(ExpressionSequenceContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitExpressionSequence(ExpressionSequenceContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterTemplateStringExpression(TemplateStringExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitTemplateStringExpression(TemplateStringExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterTernaryExpression(TernaryExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitTernaryExpression(TernaryExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterLogicalAndExpression(LogicalAndExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitLogicalAndExpression(LogicalAndExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterPowerExpression(PowerExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitPowerExpression(PowerExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterPreIncrementExpression(PreIncrementExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitPreIncrementExpression(PreIncrementExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterObjectLiteralExpression(ObjectLiteralExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitObjectLiteralExpression(ObjectLiteralExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterMetaExpression(MetaExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitMetaExpression(MetaExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterInExpression(InExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitInExpression(InExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterLogicalOrExpression(LogicalOrExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitLogicalOrExpression(LogicalOrExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterNotExpression(NotExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitNotExpression(NotExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterPreDecreaseExpression(PreDecreaseExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitPreDecreaseExpression(PreDecreaseExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterArgumentsExpression(ArgumentsExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitArgumentsExpression(ArgumentsExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterAwaitExpression(AwaitExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitAwaitExpression(AwaitExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterThisExpression(ThisExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitThisExpression(ThisExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterFunctionExpression(FunctionExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitFunctionExpression(FunctionExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterUnaryMinusExpression(UnaryMinusExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitUnaryMinusExpression(UnaryMinusExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterAssignmentExpression(AssignmentExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitAssignmentExpression(AssignmentExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterPostDecreaseExpression(PostDecreaseExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitPostDecreaseExpression(PostDecreaseExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterTypeofExpression(TypeofExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitTypeofExpression(TypeofExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterInstanceofExpression(InstanceofExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitInstanceofExpression(InstanceofExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterUnaryPlusExpression(UnaryPlusExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitUnaryPlusExpression(UnaryPlusExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterDeleteExpression(DeleteExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitDeleteExpression(DeleteExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterImportExpression(ImportExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitImportExpression(ImportExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterEqualityExpression(EqualityExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitEqualityExpression(EqualityExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterBitXOrExpression(BitXOrExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitBitXOrExpression(BitXOrExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterSuperExpression(SuperExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitSuperExpression(SuperExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterMultiplicativeExpression(MultiplicativeExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitMultiplicativeExpression(MultiplicativeExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterBitShiftExpression(BitShiftExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitBitShiftExpression(BitShiftExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterParenthesizedExpression(ParenthesizedExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitParenthesizedExpression(ParenthesizedExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterAdditiveExpression(AdditiveExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitAdditiveExpression(AdditiveExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterRelationalExpression(RelationalExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitRelationalExpression(RelationalExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterPostIncrementExpression(PostIncrementExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitPostIncrementExpression(PostIncrementExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterYieldExpression(YieldExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitYieldExpression(YieldExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterBitNotExpression(BitNotExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitBitNotExpression(BitNotExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterNewExpression(NewExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitNewExpression(NewExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterLiteralExpression(LiteralExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitLiteralExpression(LiteralExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterArrayLiteralExpression(ArrayLiteralExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitArrayLiteralExpression(ArrayLiteralExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterMemberDotExpression(MemberDotExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitMemberDotExpression(MemberDotExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterClassExpression(ClassExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitClassExpression(ClassExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterMemberIndexExpression(MemberIndexExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitMemberIndexExpression(MemberIndexExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterIdentifierExpression(IdentifierExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitIdentifierExpression(IdentifierExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterBitAndExpression(BitAndExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitBitAndExpression(BitAndExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterBitOrExpression(BitOrExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitBitOrExpression(BitOrExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterAssignmentOperatorExpression(AssignmentOperatorExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitAssignmentOperatorExpression(AssignmentOperatorExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterVoidExpression(VoidExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitVoidExpression(VoidExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterCoalesceExpression(CoalesceExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitCoalesceExpression(CoalesceExpressionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterAssignable(AssignableContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitAssignable(AssignableContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterObjectLiteral(ObjectLiteralContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitObjectLiteral(ObjectLiteralContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterFunctionDecl(FunctionDeclContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitFunctionDecl(FunctionDeclContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterAnoymousFunctionDecl(AnoymousFunctionDeclContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitAnoymousFunctionDecl(AnoymousFunctionDeclContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterArrowFunction(ArrowFunctionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitArrowFunction(ArrowFunctionContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterArrowFunctionParameters(ArrowFunctionParametersContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitArrowFunctionParameters(ArrowFunctionParametersContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterArrowFunctionBody(ArrowFunctionBodyContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitArrowFunctionBody(ArrowFunctionBodyContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterAssignmentOperator(AssignmentOperatorContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitAssignmentOperator(AssignmentOperatorContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterLiteral(LiteralContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitLiteral(LiteralContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterNumericLiteral(NumericLiteralContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitNumericLiteral(NumericLiteralContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterBigintLiteral(BigintLiteralContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitBigintLiteral(BigintLiteralContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterGetter(GetterContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitGetter(GetterContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterSetter(SetterContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitSetter(SetterContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterIdentifierName(IdentifierNameContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitIdentifierName(IdentifierNameContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterIdentifier(IdentifierContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitIdentifier(IdentifierContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterReservedWord(ReservedWordContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitReservedWord(ReservedWordContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterKeyword(KeywordContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitKeyword(KeywordContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterLet_(Let_Context ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitLet_(Let_Context ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void enterEos(EosContext ctx) {
		// TODO Auto-generated method stub
		
	}

	
	public void exitEos(EosContext ctx) {
		// TODO Auto-generated method stub
		
	}

}
