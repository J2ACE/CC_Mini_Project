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

public class StrengthReducer implements OptimizationRule {

    @Override
    public String getRuleId() {
        return "OPT005";
    }

    @Override
    public String getName() {
        return "Strength Reduction";
    }

    @Override
    public List<Diagnostic> optimize(ProgramNode ast, SymbolTable symbolTable, List<ControlFlowGraph> cfgs) {
        List<Diagnostic> diagnostics = new ArrayList<>();

        ast.accept(new BaseASTVisitor<Void, Void>() {
            @Override
            public Void visit(BinaryExpr b, Void ctx) {
                checkStrengthReduction(b, diagnostics);
                return super.visit(b, ctx);
            }
        }, null);

        return diagnostics;
    }

    private void checkStrengthReduction(BinaryExpr b, List<Diagnostic> diagnostics) {
        String op = b.getOperator();
        ExpressionNode left = b.getLeft();
        ExpressionNode right = b.getRight();

        if (op.equals("*")) {
            // Check if one operand is a power of 2
            Integer p2Right = getPowerOfTwo(right);
            Integer p2Left = getPowerOfTwo(left);

            if (p2Right != null && p2Right > 0) {
                String varName = formatNode(left);
                String orig = varName + " * " + ((LiteralNode) right).getRawValue();
                String repl = p2Right == 1 ? varName + " << 1" : varName + " << " + p2Right;
                diagnostics.add(new Diagnostic(
                        "OPT005",
                        Category.OPTIMIZATION,
                        Severity.OPTIMIZATION,
                        "Strength reduction: '" + orig + "'",
                        "Multiplication by power of 2 (2^" + p2Right + ") can be replaced with a single-cycle bitwise shift operation.",
                        "Replace '" + orig + "' with '" + repl + "'.",
                        b.getPosition().getLine(),
                        b.getPosition().getColumn(),
                        b.getPosition().getLength(),
                        orig,
                        repl,
                        "SAFE"
                ));
            } else if (p2Left != null && p2Left > 0) {
                String varName = formatNode(right);
                String orig = ((LiteralNode) left).getRawValue() + " * " + varName;
                String repl = p2Left == 1 ? varName + " << 1" : varName + " << " + p2Left;
                diagnostics.add(new Diagnostic(
                        "OPT005",
                        Category.OPTIMIZATION,
                        Severity.OPTIMIZATION,
                        "Strength reduction: '" + orig + "'",
                        "Multiplication by power of 2 (2^" + p2Left + ") can be replaced with a bitwise shift operation.",
                        "Replace '" + orig + "' with '" + repl + "'.",
                        b.getPosition().getLine(),
                        b.getPosition().getColumn(),
                        b.getPosition().getLength(),
                        orig,
                        repl,
                        "SAFE"
                ));
            }
        } else if (op.equals("/")) {
            Integer p2Right = getPowerOfTwo(right);
            if (p2Right != null && p2Right > 0) {
                String varName = formatNode(left);
                String orig = varName + " / " + ((LiteralNode) right).getRawValue();
                String repl = varName + " >> " + p2Right;
                diagnostics.add(new Diagnostic(
                        "OPT005",
                        Category.OPTIMIZATION,
                        Severity.OPTIMIZATION,
                        "Strength reduction: '" + orig + "'",
                        "Division by power of 2 can be replaced with an arithmetic right shift.",
                        "Replace '" + orig + "' with '" + repl + "'.",
                        b.getPosition().getLine(),
                        b.getPosition().getColumn(),
                        b.getPosition().getLength(),
                        orig,
                        repl,
                        "CONDITIONALLY_SAFE"
                ));
            }
        }
    }

    private Integer getPowerOfTwo(ExpressionNode node) {
        if (node instanceof LiteralNode) {
            LiteralNode lit = (LiteralNode) node;
            if (lit.getRawValue() instanceof Integer) {
                int val = (Integer) lit.getRawValue();
                if (val > 0 && (val & (val - 1)) == 0) {
                    // It is a power of 2; compute log2
                    int shift = 0;
                    while ((1 << shift) < val) {
                        shift++;
                    }
                    return shift;
                }
            }
        }
        return null;
    }

    private String formatNode(ExpressionNode node) {
        if (node instanceof VarRefNode) return ((VarRefNode) node).getVariableName();
        if (node instanceof LiteralNode) return String.valueOf(((LiteralNode) node).getRawValue());
        return "expr";
    }
}
