package com.codeinsight.compiler.ast.node;

import com.codeinsight.compiler.ast.visitor.ASTVisitor;
import com.codeinsight.compiler.symbol.Type;
import com.fasterxml.jackson.annotation.JsonProperty;

public class VarDeclNode extends StatementNode {
    private final Type type;
    private final String name;
    private final ExpressionNode initializer;

    public VarDeclNode(SourcePosition position, Type type, String name, ExpressionNode initializer) {
        super(position);
        this.type = type;
        this.name = name;
        this.initializer = initializer;
    }

    @JsonProperty("type")
    public Type getType() {
        return type;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("initializer")
    public ExpressionNode getInitializer() {
        return initializer;
    }

    @Override
    public <R, C> R accept(ASTVisitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }
}
