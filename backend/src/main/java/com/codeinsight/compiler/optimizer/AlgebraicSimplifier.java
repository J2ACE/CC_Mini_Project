package com.codeinsight.compiler.optimizer;

import com.codeinsight.compiler.ast.node.*;
import com.codeinsight.compiler.ast.visitor.BaseASTVisitor;
import com.codeinsight.compiler.cfg.ControlFlowGraph;
import com.codeinsight.compiler.diagnostic.Category;
import com.codeinsight.compiler.diagnostic.Diagnostic;
import com.codeinsight.compiler.diagnostic.Severity;
import com.codeinsight.compiler.symbol.SymbolTable;

import java.util.ArrayList;
import java.util.List;

public class AlgebraicSimplifier implements OptimizationRule {

    @Override
    public String getRuleId() {
        return "OPT003";
    }

    @Override
    public String getName() {
        return "Algebraic Simplification";
    }

    @Override
    public List<Diagnostic> optimize(ProgramNode ast, SymbolTable symbolTable, List<ControlFlowGraph> cfgs) {
        List<Diagnostic> diagnostics = new ArrayList<>();

        ast.accept(new BaseASTVisitor<Void, Void>() {
            @Override
            public Void visit(BinaryExpr b, Void ctx) {
                checkBinary(b, diagnostics);
                return super.visit(b, ctx);
            }

            @Override
            public Void visit(UnaryExpr u, Void ctx) {
                checkUnary(u, diagnostics);
                return super.visit(u, ctx);
            }
        }, null);

        return diagnostics;
    }

    private void checkBinary(BinaryExpr b, List<Diagnostic> diagnostics) {
        String op = b.getOperator();
        ExpressionNode left = b.getLeft();
        ExpressionNode right = b.getRight();

        boolean leftZero = isLiteralInt(left, 0);
        boolean rightZero = isLiteralInt(right, 0);
        boolean leftOne = isLiteralInt(left, 1);
        boolean rightOne = isLiteralInt(right, 1);

        String simplified = null;
        String explanation = null;

        if (op.equals("+")) {
            if (rightZero) {
                simplified = formatNode(left);
                explanation = "Adding zero (x + 0) is an identity operation.";
            } else if (leftZero) {
                simplified = formatNode(right);
                explanation = "Adding zero (0 + x) is an identity operation.";
            }
        } else if (op.equals("-")) {
            if (rightZero) {
                simplified = formatNode(left);
                explanation = "Subtracting zero (x - 0) is an identity operation.";
            }
        } else if (op.equals("*")) {
            if (rightOne) {
                simplified = formatNode(left);
                explanation = "Multiplying by one (x * 1) is an identity operation.";
            } else if (leftOne) {
                simplified = formatNode(right);
                explanation = "Multiplying by one (1 * x) is an identity operation.";
            } else if (rightZero || leftZero) {
                simplified = "0";
                explanation = "Multiplying by zero (x * 0) results in zero.";
            }
        } else if (op.equals("/")) {
            if (rightOne) {
                simplified = formatNode(left);
                explanation = "Dividing by one (x / 1) is an identity operation.";
            }
        }

        if (simplified != null) {
            String orig = formatNode(left) + " " + op + " " + formatNode(right);
            diagnostics.add(new Diagnostic(
                    "OPT003",
                    Category.OPTIMIZATION,
                    Severity.OPTIMIZATION,
                    "Algebraic simplification: '" + orig + "'",
                    explanation,
                    "Simplify expression to '" + simplified + "'.",
                    b.getPosition().getLine(),
                    b.getPosition().getColumn(),
                    b.getPosition().getLength(),
                    orig,
                    simplified,
                    "SAFE"
            ));
        }
    }

    private void checkUnary(UnaryExpr u, List<Diagnostic> diagnostics) {
        if (u.getOperator().equals("!") && u.getOperand() instanceof UnaryExpr) {
            UnaryExpr inner = (UnaryExpr) u.getOperand();
            if (inner.getOperator().equals("!")) {
                String target = formatNode(inner.getOperand());
                diagnostics.add(new Diagnostic(
                        "OPT003",
                        Category.OPTIMIZATION,
                        Severity.OPTIMIZATION,
                        "Double negation simplification: '!(! " + target + ")'",
                        "Double boolean negation '!(!x)' cancels out to 'x'.",
                        "Simplify to '" + target + "'.",
                        u.getPosition().getLine(),
                        u.getPosition().getColumn(),
                        u.getPosition().getLength(),
                        "!(! " + target + ")",
                        target,
                        "SAFE"
                ));
            }
        }
    }

    private boolean isLiteralInt(ExpressionNode node, int expected) {
        if (node instanceof LiteralNode) {
            LiteralNode lit = (LiteralNode) node;
            if (lit.getRawValue() instanceof Integer) {
                return ((Integer) lit.getRawValue()) == expected;
            }
        }
        return false;
    }

    private String formatNode(ExpressionNode node) {
        if (node instanceof VarRefNode) return ((VarRefNode) node).getVariableName();
        if (node instanceof LiteralNode) return String.valueOf(((LiteralNode) node).getRawValue());
        if (node instanceof BinaryExpr) {
            BinaryExpr b = (BinaryExpr) node;
            return formatNode(b.getLeft()) + " " + b.getOperator() + " " + formatNode(b.getRight());
        }
        return "expr";
    }
}
