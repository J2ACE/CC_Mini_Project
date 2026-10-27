package com.codeinsight.compiler.ast.node;

import com.codeinsight.compiler.ast.visitor.ASTVisitor;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AssignNode extends StatementNode {
    private final String targetVar;
    private final ExpressionNode value;

    public AssignNode(SourcePosition position, String targetVar, ExpressionNode value) {
        super(position);
        this.targetVar = targetVar;
        this.value = value;
    }

    @JsonProperty("targetVar")
    public String getTargetVar() {
        return targetVar;
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
