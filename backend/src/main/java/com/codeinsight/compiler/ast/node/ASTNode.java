package com.codeinsight.compiler.ast.node;

import com.codeinsight.compiler.ast.visitor.ASTVisitor;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "nodeType")
public abstract class ASTNode {
    private final SourcePosition position;

    protected ASTNode(SourcePosition position) {
        this.position = position;
    }

    @JsonProperty("position")
    public SourcePosition getPosition() {
        return position;
    }

    public abstract <R, C> R accept(ASTVisitor<R, C> visitor, C context);
}
