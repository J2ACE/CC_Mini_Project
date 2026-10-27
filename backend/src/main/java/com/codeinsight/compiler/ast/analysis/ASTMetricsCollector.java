package com.codeinsight.compiler.ast.analysis;

import com.codeinsight.compiler.ast.node.*;

public class ASTMetricsCollector {
    private int totalNodes = 0;
    private int statements = 0;
    private int varDecls = 0;
    private int conditionalBranches = 0;
    private int loopConstructs = 0;
    private int maxNestingDepth = 0;
    private int decisionPoints = 0;
    private int astDepth = 0;

    public static ASTMetricsCollector analyze(ProgramNode ast) {
        ASTMetricsCollector collector = new ASTMetricsCollector();
        if (ast != null) {
            collector.astDepth = collector.visitNode(ast, 0);
        }
        return collector;
    }

    private int visitNode(ASTNode node, int nestingDepth) {
        if (node == null) return 0;
        totalNodes++;

        int childDepth = 0;

        if (node instanceof ProgramNode) {
            ProgramNode p = (ProgramNode) node;
            for (VarDeclNode g : p.getGlobals()) {
                childDepth = Math.max(childDepth, visitNode(g, nestingDepth));
            }
            for (FunctionNode f : p.getFunctions()) {
                childDepth = Math.max(childDepth, visitNode(f, nestingDepth));
            }
        } else if (node instanceof FunctionNode) {
            FunctionNode f = (FunctionNode) node;
            for (ParamNode p : f.getParameters()) {
                childDepth = Math.max(childDepth, visitNode(p, nestingDepth));
            }
            childDepth = Math.max(childDepth, visitNode(f.getBody(), nestingDepth));
        } else if (node instanceof BlockNode) {
            BlockNode b = (BlockNode) node;
            for (StatementNode s : b.getStatements()) {
                childDepth = Math.max(childDepth, visitNode(s, nestingDepth));
            }
        } else if (node instanceof VarDeclNode) {
            VarDeclNode v = (VarDeclNode) node;
            statements++;
            varDecls++;
            if (v.getInitializer() != null) {
                childDepth = Math.max(childDepth, visitNode(v.getInitializer(), nestingDepth));
            }
        } else if (node instanceof AssignNode) {
            AssignNode a = (AssignNode) node;
            statements++;
            childDepth = Math.max(childDepth, visitNode(a.getValue(), nestingDepth));
        } else if (node instanceof IfNode) {
            IfNode ifNode = (IfNode) node;
            statements++;
            conditionalBranches++;
            decisionPoints++;
            int nextNesting = nestingDepth + 1;
            maxNestingDepth = Math.max(maxNestingDepth, nextNesting);

            childDepth = Math.max(childDepth, visitNode(ifNode.getCondition(), nestingDepth));
            childDepth = Math.max(childDepth, visitNode(ifNode.getThenBranch(), nextNesting));
            if (ifNode.getElseBranch() != null) {
                childDepth = Math.max(childDepth, visitNode(ifNode.getElseBranch(), nextNesting));
            }
        } else if (node instanceof WhileNode) {
            WhileNode w = (WhileNode) node;
            statements++;
            loopConstructs++;
            decisionPoints++;
            int nextNesting = nestingDepth + 1;
            maxNestingDepth = Math.max(maxNestingDepth, nextNesting);

            childDepth = Math.max(childDepth, visitNode(w.getCondition(), nestingDepth));
            childDepth = Math.max(childDepth, visitNode(w.getBody(), nextNesting));
        } else if (node instanceof ForNode) {
            ForNode f = (ForNode) node;
            statements++;
            loopConstructs++;
            decisionPoints++;
            int nextNesting = nestingDepth + 1;
            maxNestingDepth = Math.max(maxNestingDepth, nextNesting);

            if (f.getInit() != null) childDepth = Math.max(childDepth, visitNode(f.getInit(), nestingDepth));
            if (f.getCondition() != null) childDepth = Math.max(childDepth, visitNode(f.getCondition(), nestingDepth));
            if (f.getUpdate() != null) childDepth = Math.max(childDepth, visitNode(f.getUpdate(), nestingDepth));
            childDepth = Math.max(childDepth, visitNode(f.getBody(), nextNesting));
        } else if (node instanceof ReturnNode) {
            ReturnNode r = (ReturnNode) node;
            statements++;
            if (r.getValue() != null) {
                childDepth = Math.max(childDepth, visitNode(r.getValue(), nestingDepth));
            }
        } else if (node instanceof ExprStmtNode) {
            statements++;
            childDepth = Math.max(childDepth, visitNode(((ExprStmtNode) node).getExpression(), nestingDepth));
        } else if (node instanceof BinaryExpr) {
            BinaryExpr b = (BinaryExpr) node;
            if ("&&".equals(b.getOperator()) || "||".equals(b.getOperator())) {
                decisionPoints++;
            }
            childDepth = Math.max(childDepth, visitNode(b.getLeft(), nestingDepth));
            childDepth = Math.max(childDepth, visitNode(b.getRight(), nestingDepth));
        } else if (node instanceof UnaryExpr) {
            UnaryExpr u = (UnaryExpr) node;
            childDepth = Math.max(childDepth, visitNode(u.getOperand(), nestingDepth));
        } else if (node instanceof CallNode) {
            CallNode c = (CallNode) node;
            for (ExpressionNode arg : c.getArguments()) {
                childDepth = Math.max(childDepth, visitNode(arg, nestingDepth));
            }
        }

        return 1 + childDepth;
    }

    public int getTotalNodes() {
        return totalNodes;
    }

    public int getStatements() {
        return statements;
    }

    public int getVarDecls() {
        return varDecls;
    }

    public int getConditionalBranches() {
        return conditionalBranches;
    }

    public int getLoopConstructs() {
        return loopConstructs;
    }

    public int getMaxNestingDepth() {
        return Math.max(1, maxNestingDepth);
    }

    public int getDecisionPoints() {
        return decisionPoints;
    }

    public int getCyclomaticComplexity() {
        return 1 + decisionPoints;
    }

    public int getAstDepth() {
        return astDepth;
    }
}
