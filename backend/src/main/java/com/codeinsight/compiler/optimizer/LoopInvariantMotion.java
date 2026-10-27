package com.codeinsight.compiler.optimizer;

import com.codeinsight.compiler.ast.node.*;
import com.codeinsight.compiler.ast.visitor.BaseASTVisitor;
import com.codeinsight.compiler.cfg.ControlFlowGraph;
import com.codeinsight.compiler.diagnostic.Category;
import com.codeinsight.compiler.diagnostic.Diagnostic;
import com.codeinsight.compiler.diagnostic.Severity;
import com.codeinsight.compiler.symbol.SymbolTable;

import java.util.*;

public class LoopInvariantMotion implements OptimizationRule {

    @Override
    public String getRuleId() {
        return "OPT006";
    }

    @Override
    public String getName() {
        return "Loop-Invariant Code Motion";
    }

    @Override
    public List<Diagnostic> optimize(ProgramNode ast, SymbolTable symbolTable, List<ControlFlowGraph> cfgs) {
        List<Diagnostic> diagnostics = new ArrayList<>();

        ast.accept(new BaseASTVisitor<Void, Void>() {
            @Override
            public Void visit(WhileNode whileNode, Void ctx) {
                checkLoop(whileNode.getBody(), whileNode.getPosition().getLine(), diagnostics);
                return super.visit(whileNode, ctx);
            }

            @Override
            public Void visit(ForNode forNode, Void ctx) {
                checkLoop(forNode.getBody(), forNode.getPosition().getLine(), diagnostics);
                return super.visit(forNode, ctx);
            }
        }, null);

        return diagnostics;
    }

    private void checkLoop(StatementNode body, int loopLine, List<Diagnostic> diagnostics) {
        if (body == null) return;

        // 1. Collect all variables modified inside the loop
        Set<String> modifiedVars = new HashSet<>();
        body.accept(new BaseASTVisitor<Void, Void>() {
            @Override
            public Void visit(AssignNode an, Void ctx) {
                modifiedVars.add(an.getTargetVar());
                return super.visit(an, ctx);
            }

            @Override
            public Void visit(VarDeclNode vd, Void ctx) {
                modifiedVars.add(vd.getName());
                return super.visit(vd, ctx);
            }
        }, null);

        // 2. Scan for expressions inside loop where all operands are NOT modified inside loop
        body.accept(new BaseASTVisitor<Void, Void>() {
            @Override
            public Void visit(BinaryExpr b, Void ctx) {
                Set<String> usedVars = new HashSet<>();
                b.accept(new BaseASTVisitor<Void, Void>() {
                    @Override
                    public Void visit(VarRefNode ref, Void c) {
                        usedVars.add(ref.getVariableName());
                        return null;
                    }
                }, null);

                // If non-trivial expression uses variables, but NONE of them are modified in loop
                if (!usedVars.isEmpty() && Collections.disjoint(usedVars, modifiedVars)) {
                    String exprStr = formatExpr(b);
                    diagnostics.add(new Diagnostic(
                            "OPT006",
                            Category.OPTIMIZATION,
                            Severity.OPTIMIZATION,
                            "Loop-invariant expression detected: '" + exprStr + "'",
                            "Operands " + usedVars + " are never modified inside the loop body. Computing this expression on every iteration is redundant.",
                            "Hoist computation '" + exprStr + "' outside the loop header.",
                            b.getPosition().getLine(),
                            b.getPosition().getColumn(),
                            b.getPosition().getLength(),
                            exprStr,
                            "// hoist: int inv = " + exprStr + ";",
                            "SAFE"
                    ));
                }
                return super.visit(b, ctx);
            }
        }, null);
    }

    private String formatExpr(BinaryExpr b) {
        String l = b.getLeft() instanceof VarRefNode ? ((VarRefNode) b.getLeft()).getVariableName() : (b.getLeft() instanceof LiteralNode ? String.valueOf(((LiteralNode) b.getLeft()).getRawValue()) : "expr");
        String r = b.getRight() instanceof VarRefNode ? ((VarRefNode) b.getRight()).getVariableName() : (b.getRight() instanceof LiteralNode ? String.valueOf(((LiteralNode) b.getRight()).getRawValue()) : "expr");
        return l + " " + b.getOperator() + " " + r;
    }
}
