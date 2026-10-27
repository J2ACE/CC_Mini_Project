package com.codeinsight.compiler.ast.node;

import com.codeinsight.compiler.ast.visitor.ASTVisitor;
import com.codeinsight.compiler.symbol.Type;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

public class FunctionNode extends ASTNode {
    private final Type returnType;
    private final String name;
    private final List<ParamNode> parameters;
    private final BlockNode body;

    public FunctionNode(SourcePosition position, Type returnType, String name, List<ParamNode> parameters, BlockNode body) {
        super(position);
        this.returnType = returnType;
        this.name = name;
        this.parameters = parameters != null ? parameters : Collections.emptyList();
        this.body = body;
    }

    @JsonProperty("returnType")
    public Type getReturnType() {
        return returnType;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("parameters")
    public List<ParamNode> getParameters() {
        return parameters;
    }

    @JsonProperty("body")
    public BlockNode getBody() {
        return body;
    }

    @Override
    public <R, C> R accept(ASTVisitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }
}
