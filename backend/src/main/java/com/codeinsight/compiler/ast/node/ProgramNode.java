package com.codeinsight.compiler.ast.node;

import com.codeinsight.compiler.ast.visitor.ASTVisitor;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

public class ProgramNode extends ASTNode {
    private final List<VarDeclNode> globals;
    private final List<FunctionNode> functions;

    public ProgramNode(SourcePosition position, List<VarDeclNode> globals, List<FunctionNode> functions) {
        super(position);
        this.globals = globals != null ? globals : Collections.emptyList();
        this.functions = functions != null ? functions : Collections.emptyList();
    }

    @JsonProperty("globals")
    public List<VarDeclNode> getGlobals() {
        return globals;
    }

    @JsonProperty("functions")
    public List<FunctionNode> getFunctions() {
        return functions;
    }

    @Override
    public <R, C> R accept(ASTVisitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }
}
