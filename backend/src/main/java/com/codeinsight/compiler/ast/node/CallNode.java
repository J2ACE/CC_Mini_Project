package com.codeinsight.compiler.ast.node;

import com.codeinsight.compiler.ast.visitor.ASTVisitor;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

public class CallNode extends ExpressionNode {
    private final String functionName;
    private final List<ExpressionNode> arguments;

    public CallNode(SourcePosition position, String functionName, List<ExpressionNode> arguments) {
        super(position);
        this.functionName = functionName;
        this.arguments = arguments != null ? arguments : Collections.emptyList();
    }

    @JsonProperty("functionName")
    public String getFunctionName() {
        return functionName;
    }

    @JsonProperty("arguments")
    public List<ExpressionNode> getArguments() {
        return arguments;
    }

    @Override
    public <R, C> R accept(ASTVisitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }
}
