package com.codeinsight.compiler.ast.node;

import com.codeinsight.compiler.ast.visitor.ASTVisitor;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

public class BlockNode extends StatementNode {
    private final List<StatementNode> statements;

    public BlockNode(SourcePosition position, List<StatementNode> statements) {
        super(position);
        this.statements = statements != null ? statements : Collections.emptyList();
    }

    @JsonProperty("statements")
    public List<StatementNode> getStatements() {
        return statements;
    }

    @Override
    public <R, C> R accept(ASTVisitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }
}
