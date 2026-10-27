package com.codeinsight.compiler.ast.node;

import com.codeinsight.compiler.ast.visitor.ASTVisitor;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BinaryExpr extends ExpressionNode {
    private final String operator;
    private final ExpressionNode left;
    private final ExpressionNode right;

    public BinaryExpr(SourcePosition position, String operator, ExpressionNode left, ExpressionNode right) {
        super(position);
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    @JsonProperty("operator")
    public String getOperator() {
        return operator;
    }

    @JsonProperty("left")
    public ExpressionNode getLeft() {
        return left;
    }

    @JsonProperty("right")
    public ExpressionNode getRight() {
        return right;
    }

    @Override
    public <R, C> R accept(ASTVisitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }
}
