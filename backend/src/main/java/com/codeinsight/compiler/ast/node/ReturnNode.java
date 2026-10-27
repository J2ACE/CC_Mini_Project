package com.codeinsight.compiler.ast.node;

import com.codeinsight.compiler.ast.visitor.ASTVisitor;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReturnNode extends StatementNode {
    private final ExpressionNode value;

    public ReturnNode(SourcePosition position, ExpressionNode value) {
        super(position);
        this.value = value;
    }

    @JsonProperty("value")
    public ExpressionNode getValue() {
        return value;
    }

    @Override
    public <R, C> R accept(ASTVisitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }
}
