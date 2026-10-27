package com.codeinsight.compiler.ast.node;

import com.codeinsight.compiler.ast.visitor.ASTVisitor;
import com.codeinsight.compiler.symbol.Type;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ParamNode extends ASTNode {
    private final Type type;
    private final String name;

    public ParamNode(SourcePosition position, Type type, String name) {
        super(position);
        this.type = type;
        this.name = name;
    }

    @JsonProperty("type")
    public Type getType() {
        return type;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @Override
    public <R, C> R accept(ASTVisitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }
}
