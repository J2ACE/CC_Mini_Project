package com.codeinsight.compiler.ast.node;

import com.codeinsight.compiler.ast.visitor.ASTVisitor;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ForNode extends StatementNode {
    private final ASTNode init;
    private final ExpressionNode condition;
    private final ASTNode update;
    private final StatementNode body;

    public ForNode(SourcePosition position, ASTNode init, ExpressionNode condition, ASTNode update, StatementNode body) {
        super(position);
        this.init = init;
        this.condition = condition;
        this.update = update;
        this.body = body;
    }

    @JsonProperty("init")
    public ASTNode getInit() {
        return init;
    }

    @JsonProperty("condition")
    public ExpressionNode getCondition() {
        return condition;
    }

    @JsonProperty("update")
    public ASTNode getUpdate() {
        return update;
    }

    @JsonProperty("body")
    public StatementNode getBody() {
        return body;
    }

    @Override
    public <R, C> R accept(ASTVisitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }
}
