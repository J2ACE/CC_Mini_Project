package com.codeinsight.compiler.ast.node;

import com.codeinsight.compiler.symbol.Type;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class ExpressionNode extends ASTNode {
    private Type inferredType = Type.UNKNOWN;

    protected ExpressionNode(SourcePosition position) {
        super(position);
    }

    @JsonProperty("inferredType")
    public Type getInferredType() {
        return inferredType;
    }

    public void setInferredType(Type inferredType) {
        this.inferredType = inferredType;
    }
}
