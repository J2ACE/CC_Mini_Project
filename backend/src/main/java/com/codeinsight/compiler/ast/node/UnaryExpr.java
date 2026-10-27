package com.codeinsight.compiler.ast.node;

import com.codeinsight.compiler.ast.visitor.ASTVisitor;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UnaryExpr extends ExpressionNode {
    private final String operator;
    private final ExpressionNode operand;

    public UnaryExpr(SourcePosition position, String operator, ExpressionNode operand) {
        super(position);
        this.operator = operator;
        this.operand = operand;
    }

    @JsonProperty("operator")
    public String getOperator() {
        return operator;
    }

    @JsonProperty("operand")
    public ExpressionNode getOperand() {
        return operand;
    }

    @Override
    public <R, C> R accept(ASTVisitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }
}
