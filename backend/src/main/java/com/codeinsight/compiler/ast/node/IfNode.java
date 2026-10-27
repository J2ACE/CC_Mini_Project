package com.codeinsight.compiler.ast.node;

import com.codeinsight.compiler.ast.visitor.ASTVisitor;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IfNode extends StatementNode {
    private final ExpressionNode condition;
    private final StatementNode thenBranch;
    private final StatementNode elseBranch;

    public IfNode(SourcePosition position, ExpressionNode condition, StatementNode thenBranch, StatementNode elseBranch) {
        super(position);
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    @JsonProperty("condition")
    public ExpressionNode getCondition() {
        return condition;
    }

    @JsonProperty("thenBranch")
    public StatementNode getThenBranch() {
        return thenBranch;
    }

    @JsonProperty("elseBranch")
    public StatementNode getElseBranch() {
        return elseBranch;
    }

    @Override
    public <R, C> R accept(ASTVisitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }
}
