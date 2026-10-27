package com.codeinsight.compiler.optimizer;

import com.codeinsight.compiler.ast.node.*;
import com.codeinsight.compiler.ast.visitor.BaseASTVisitor;
import com.codeinsight.compiler.cfg.ControlFlowGraph;
import com.codeinsight.compiler.diagnostic.Category;
import com.codeinsight.compiler.diagnostic.Diagnostic;
import com.codeinsight.compiler.diagnostic.Severity;
import com.codeinsight.compiler.symbol.SymbolTable;
import com.codeinsight.compiler.symbol.Type;

import java.util.ArrayList;
import java.util.List;

public class ConstantFolder implements OptimizationRule {

    @Override
    public String getRuleId() {
        return "OPT001";
    }

    @Override
    public String getName() {
        return "Constant Folding";
    }

    @Override
    public List<Diagnostic> optimize(ProgramNode ast, SymbolTable symbolTable, List<ControlFlowGraph> cfgs) {
        List<Diagnostic> diagnostics = new ArrayList<>();

        ast.accept(new BaseASTVisitor<Void, Void>() {
            @Override
            public Void visit(BinaryExpr b, Void ctx) {
                // If both left and right evaluate to compile-time constants:
                Object leftVal = evalConst(b.getLeft());
                Object rightVal = evalConst(b.getRight());

                if (leftVal != null && rightVal != null) {
                    Object folded = compute(b.getOperator(), leftVal, rightVal);
                    if (folded != null) {
                        String orig = formatSnippet(b);
                        String repl = String.valueOf(folded);
                        diagnostics.add(new Diagnostic(
                                "OPT001",
                                Category.OPTIMIZATION,
                                Severity.OPTIMIZATION,
                                "Constant expression detected: " + orig,
                                "Both operands are compile-time constants. Evaluating this expression at compile-time reduces CPU arithmetic instructions at runtime.",
                                "Replace '" + orig + "' with '" + repl + "'.",
                                b.getPosition().getLine(),
                                b.getPosition().getColumn(),
                                b.getPosition().getLength(),
                                orig,
                                repl,
                                "SAFE"
                        ));
                    }
                }
                return super.visit(b, ctx);
            }
        }, null);

        return diagnostics;
    }

    private Object evalConst(ExpressionNode node) {
        if (node instanceof LiteralNode) {
            return ((LiteralNode) node).getRawValue();
        }
        if (node instanceof BinaryExpr) {
            BinaryExpr b = (BinaryExpr) node;
            Object l = evalConst(b.getLeft());
            Object r = evalConst(b.getRight());
            if (l != null && r != null) {
                return compute(b.getOperator(), l, r);
            }
        }
        return null;
    }

    private Object compute(String op, Object l, Object r) {
        if (l instanceof Integer && r instanceof Integer) {
            int a = (Integer) l;
            int b = (Integer) r;
            switch (op) {
                case "+": return a + b;
                case "-": return a - b;
                case "*": return a * b;
                case "/": return b != 0 ? a / b : null;
                case "%": return b != 0 ? a % b : null;
                case "<": return a < b;
                case "<=": return a <= b;
                case ">": return a > b;
                case ">=": return a >= b;
                case "==": return a == b;
                case "!=": return a != b;
            }
        } else if (l instanceof Boolean && r instanceof Boolean) {
            boolean a = (Boolean) l;
            boolean b = (Boolean) r;
            switch (op) {
                case "&&": return a && b;
                case "||": return a || b;
                case "==": return a == b;
                case "!=": return a != b;
            }
        }
        return null;
    }

    private String formatSnippet(BinaryExpr b) {
        String l = b.getLeft() instanceof LiteralNode ? String.valueOf(((LiteralNode) b.getLeft()).getRawValue()) : "...";
        String r = b.getRight() instanceof LiteralNode ? String.valueOf(((LiteralNode) b.getRight()).getRawValue()) : "...";
        return l + " " + b.getOperator() + " " + r;
    }
}
