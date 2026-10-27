package com.codeinsight.compiler.ast.node;

import com.codeinsight.compiler.ast.visitor.ASTVisitor;
import com.fasterxml.jackson.annotation.JsonProperty;

public class VarRefNode extends ExpressionNode {
    private final String variableName;

    public VarRefNode(SourcePosition position, String variableName) {
        super(position);
        this.variableName = variableName;
    }

    @JsonProperty("variableName")
    public String getVariableName() {
        return variableName;
    }

    @Override
    public <R, C> R accept(ASTVisitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }
}
