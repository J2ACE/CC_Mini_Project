package com.codeinsight.compiler.ast.node;

public abstract class StatementNode extends ASTNode {
    protected StatementNode(SourcePosition position) {
        super(position);
    }
}
