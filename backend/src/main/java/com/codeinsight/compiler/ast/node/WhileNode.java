package com.codeinsight.compiler.ast.node;

import com.codeinsight.compiler.ast.visitor.ASTVisitor;
import com.fasterxml.jackson.annotation.JsonProperty;

public class WhileNode extends StatementNode {
    private final ExpressionNode condition;
    private final StatementNode body;

    public WhileNode(SourcePosition position, ExpressionNode condition, StatementNode body) {
        super(position);
        this.condition = condition;
        this.body = body;
    }

    @JsonProperty("condition")
    public ExpressionNode getCondition() {
        return condition;
    }

    @JsonProperty("body")
    public StatementNode getBody() {
        return body;
    }

    @Override
    public <R, C> R accept(ASTVisitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }
}
