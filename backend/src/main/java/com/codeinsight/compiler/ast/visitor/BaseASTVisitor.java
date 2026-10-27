package com.codeinsight.compiler.ast.visitor;

import com.codeinsight.compiler.ast.node.*;

public class BaseASTVisitor<R, C> implements ASTVisitor<R, C> {
    @Override
    public R visit(ProgramNode node, C context) {
        for (VarDeclNode global : node.getGlobals()) {
            global.accept(this, context);
        }
        for (FunctionNode fn : node.getFunctions()) {
            fn.accept(this, context);
        }
        return null;
    }

    @Override
    public R visit(FunctionNode node, C context) {
        for (ParamNode p : node.getParameters()) {
            p.accept(this, context);
        }
        if (node.getBody() != null) {
            node.getBody().accept(this, context);
        }
        return null;
    }

    @Override
    public R visit(ParamNode node, C context) {
        return null;
    }

    @Override
    public R visit(BlockNode node, C context) {
        for (StatementNode stmt : node.getStatements()) {
            stmt.accept(this, context);
        }
        return null;
    }

    @Override
    public R visit(VarDeclNode node, C context) {
        if (node.getInitializer() != null) {
            node.getInitializer().accept(this, context);
        }
        return null;
    }

    @Override
    public R visit(AssignNode node, C context) {
        if (node.getValue() != null) {
            node.getValue().accept(this, context);
        }
        return null;
    }

    @Override
    public R visit(IfNode node, C context) {
        if (node.getCondition() != null) {
            node.getCondition().accept(this, context);
        }
        if (node.getThenBranch() != null) {
            node.getThenBranch().accept(this, context);
        }
        if (node.getElseBranch() != null) {
            node.getElseBranch().accept(this, context);
        }
        return null;
    }

    @Override
    public R visit(WhileNode node, C context) {
        if (node.getCondition() != null) {
            node.getCondition().accept(this, context);
        }
        if (node.getBody() != null) {
            node.getBody().accept(this, context);
        }
        return null;
    }

    @Override
    public R visit(ForNode node, C context) {
        if (node.getInit() != null) {
            node.getInit().accept(this, context);
        }
        if (node.getCondition() != null) {
            node.getCondition().accept(this, context);
        }
        if (node.getUpdate() != null) {
            node.getUpdate().accept(this, context);
        }
        if (node.getBody() != null) {
            node.getBody().accept(this, context);
        }
        return null;
    }

    @Override
    public R visit(ReturnNode node, C context) {
        if (node.getValue() != null) {
            node.getValue().accept(this, context);
        }
        return null;
    }

    @Override
    public R visit(ExprStmtNode node, C context) {
        if (node.getExpression() != null) {
            node.getExpression().accept(this, context);
        }
        return null;
    }

    @Override
    public R visit(BinaryExpr node, C context) {
        if (node.getLeft() != null) node.getLeft().accept(this, context);
        if (node.getRight() != null) node.getRight().accept(this, context);
        return null;
    }

    @Override
    public R visit(UnaryExpr node, C context) {
        if (node.getOperand() != null) node.getOperand().accept(this, context);
        return null;
    }

    @Override
    public R visit(LiteralNode node, C context) {
        return null;
    }

    @Override
    public R visit(VarRefNode node, C context) {
        return null;
    }

    @Override
    public R visit(CallNode node, C context) {
        for (ExpressionNode arg : node.getArguments()) {
            arg.accept(this, context);
        }
        return null;
    }
}
