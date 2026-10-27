package com.codeinsight.compiler.ast.node;

import com.codeinsight.compiler.ast.visitor.ASTVisitor;
import com.codeinsight.compiler.symbol.Type;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LiteralNode extends ExpressionNode {
    private final Type literalType;
    private final Object rawValue;

    public LiteralNode(SourcePosition position, Type literalType, Object rawValue) {
        super(position);
        this.literalType = literalType;
        this.rawValue = rawValue;
        setInferredType(literalType);
    }

    @JsonProperty("literalType")
    public Type getLiteralType() {
        return literalType;
    }

    @JsonProperty("rawValue")
    public Object getRawValue() {
        return rawValue;
    }

    @Override
    public <R, C> R accept(ASTVisitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }
}
