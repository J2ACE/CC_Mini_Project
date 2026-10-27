package com.codeinsight.compiler.analyzer;

import com.codeinsight.compiler.ast.node.*;
import com.codeinsight.compiler.ast.visitor.BaseASTVisitor;
import com.codeinsight.compiler.cfg.ControlFlowGraph;
import com.codeinsight.compiler.diagnostic.Category;
import com.codeinsight.compiler.diagnostic.Diagnostic;
import com.codeinsight.compiler.diagnostic.Severity;
import com.codeinsight.compiler.symbol.SymbolTable;

import java.util.ArrayList;
import java.util.List;

public class TautologicalConditionDetector implements StaticAnalysisRule {

    @Override
    public String getRuleId() {
        return "STA005";
    }

    @Override
    public String getName() {
        return "Tautological / Invariant Condition Detection";
    }

    @Override
    public List<Diagnostic> analyze(ProgramNode ast, SymbolTable symbolTable, List<ControlFlowGraph> cfgs) {
        List<Diagnostic> diagnostics = new ArrayList<>();

        ast.accept(new BaseASTVisitor<Void, Void>() {
            @Override
            public Void visit(IfNode ifNode, Void ctx) {
                checkCondition(ifNode.getCondition(), diagnostics, "if");
                return super.visit(ifNode, ctx);
            }

            @Override
            public Void visit(WhileNode whileNode, Void ctx) {
                checkCondition(whileNode.getCondition(), diagnostics, "while");
                return super.visit(whileNode, ctx);
            }
        }, null);

        return diagnostics;
    }

    private void checkCondition(ExpressionNode cond, List<Diagnostic> diagnostics, String construct) {
        if (cond == null) return;

        // Literal true / false
        if (cond instanceof LiteralNode) {
            LiteralNode lit = (LiteralNode) cond;
            diagnostics.add(new Diagnostic(
                    "STA005",
                    Category.STATIC_ANALYSIS,
                    Severity.WARNING,
                    "Constant condition in '" + construct + "': " + lit.getRawValue(),
                    "The condition is hardcoded to a constant literal, rendering one branch permanently dead.",
                    "Use a dynamic expression or remove the redundant condition.",
                    cond.getPosition().getLine(),
                    cond.getPosition().getColumn(),
                    cond.getPosition().getLength(),
                    String.valueOf(lit.getRawValue()),
                    "",
                    "SAFE"
            ));
            return;
        }

        // Tautological equality: a == a or constant comparison like 10 > 5
        if (cond instanceof BinaryExpr) {
            BinaryExpr b = (BinaryExpr) cond;
            if (b.getLeft() instanceof VarRefNode && b.getRight() instanceof VarRefNode) {
                String leftVar = ((VarRefNode) b.getLeft()).getVariableName();
                String rightVar = ((VarRefNode) b.getRight()).getVariableName();
                if (leftVar.equals(rightVar) && b.getOperator().equals("==")) {
                    diagnostics.add(new Diagnostic(
                            "STA005",
                            Category.STATIC_ANALYSIS,
                            Severity.WARNING,
                            "Tautological condition: '" + leftVar + " == " + rightVar + "'",
                            "Comparing a variable to itself with '==' is always true.",
                            "Review comparison logic.",
                            cond.getPosition().getLine(),
                            cond.getPosition().getColumn(),
                            cond.getPosition().getLength(),
                            leftVar + " == " + rightVar,
                            "",
                            "SAFE"
                    ));
                    return;
                }
            }

            // Both operands are literals
            if (b.getLeft() instanceof LiteralNode && b.getRight() instanceof LiteralNode) {
                diagnostics.add(new Diagnostic(
                        "STA005",
                        Category.STATIC_ANALYSIS,
                        Severity.WARNING,
                        "Invariant constant condition: '" + b.getOperator() + "'",
                        "Both operands of the condition are constants evaluated at compile time.",
                        "Simplify the condition.",
                        cond.getPosition().getLine(),
                        cond.getPosition().getColumn(),
                        cond.getPosition().getLength(),
                        b.getOperator(),
                        "",
                        "SAFE"
                ));
            }
        }
    }
}
