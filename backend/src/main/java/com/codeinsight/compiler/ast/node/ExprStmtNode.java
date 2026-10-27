package com.codeinsight.compiler.ast.node;

import com.codeinsight.compiler.ast.visitor.ASTVisitor;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ExprStmtNode extends StatementNode {
    private final ExpressionNode expression;

    public ExprStmtNode(SourcePosition position, ExpressionNode expression) {
        super(position);
        this.expression = expression;
    }

    @JsonProperty("expression")
    public ExpressionNode getExpression() {
        return expression;
    }

    @Override
    public <R, C> R accept(ASTVisitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }
}
