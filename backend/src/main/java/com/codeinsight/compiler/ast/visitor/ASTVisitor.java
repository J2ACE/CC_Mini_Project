package com.codeinsight.compiler.ast.visitor;

import com.codeinsight.compiler.ast.node.*;

public interface ASTVisitor<R, C> {
    R visit(ProgramNode node, C context);
    R visit(FunctionNode node, C context);
    R visit(ParamNode node, C context);
    R visit(BlockNode node, C context);
    R visit(VarDeclNode node, C context);
    R visit(AssignNode node, C context);
    R visit(IfNode node, C context);
    R visit(WhileNode node, C context);
    R visit(ForNode node, C context);
    R visit(ReturnNode node, C context);
    R visit(ExprStmtNode node, C context);
    R visit(BinaryExpr node, C context);
    R visit(UnaryExpr node, C context);
    R visit(LiteralNode node, C context);
    R visit(VarRefNode node, C context);
    R visit(CallNode node, C context);
}
