package com.codeinsight.compiler.optimizer;

import com.codeinsight.compiler.ast.node.*;
import com.codeinsight.compiler.ast.visitor.BaseASTVisitor;
import com.codeinsight.compiler.cfg.ControlFlowGraph;
import com.codeinsight.compiler.diagnostic.Category;
import com.codeinsight.compiler.diagnostic.Diagnostic;
import com.codeinsight.compiler.diagnostic.Severity;
import com.codeinsight.compiler.symbol.SymbolTable;

import java.util.*;

public class CommonSubexpressionEliminator implements OptimizationRule {

    @Override
    public String getRuleId() {
        return "OPT004";
    }

    @Override
    public String getName() {
        return "Common Subexpression Detection";
    }

    @Override
    public List<Diagnostic> optimize(ProgramNode ast, SymbolTable symbolTable, List<ControlFlowGraph> cfgs) {
        List<Diagnostic> diagnostics = new ArrayList<>();

        for (FunctionNode fn : ast.getFunctions()) {
            Map<String, List<BinaryExpr>> exprOccurrences = new HashMap<>();

            fn.accept(new BaseASTVisitor<Void, Void>() {
                @Override
                public Void visit(BinaryExpr b, Void ctx) {
                    // Only consider non-trivial expressions involving variables
                    if (containsVar(b)) {
                        String key = formatExpr(b);
                        exprOccurrences.computeIfAbsent(key, k -> new ArrayList<>()).add(b);
                    }
                    return super.visit(b, ctx);
                }
            }, null);

            for (Map.Entry<String, List<BinaryExpr>> entry : exprOccurrences.entrySet()) {
                List<BinaryExpr> list = entry.getValue();
                if (list.size() > 1) {
                    BinaryExpr first = list.get(0);
                    for (int i = 1; i < list.size(); i++) {
                        BinaryExpr repeat = list.get(i);
                        diagnostics.add(new Diagnostic(
                                "OPT004",
                                Category.OPTIMIZATION,
                                Severity.OPTIMIZATION,
                                "Common subexpression detected: '" + entry.getKey() + "'",
                                "The identical subexpression '" + entry.getKey() + "' was already evaluated at line " + first.getPosition().getLine() + ". Recomputing it consumes unnecessary arithmetic cycles.",
                                "Assign '" + entry.getKey() + "' to a temporary variable and reuse it.",
                                repeat.getPosition().getLine(),
                                repeat.getPosition().getColumn(),
                                repeat.getPosition().getLength(),
                                entry.getKey(),
                                "temp",
                                "CONDITIONALLY_SAFE"
                        ));
                    }
                }
            }
        }

        return diagnostics;
    }

    private boolean containsVar(BinaryExpr b) {
        return b.getLeft() instanceof VarRefNode || b.getRight() instanceof VarRefNode;
    }

    private String formatExpr(BinaryExpr b) {
        String l = b.getLeft() instanceof VarRefNode ? ((VarRefNode) b.getLeft()).getVariableName() : (b.getLeft() instanceof LiteralNode ? String.valueOf(((LiteralNode) b.getLeft()).getRawValue()) : "...");
        String r = b.getRight() instanceof VarRefNode ? ((VarRefNode) b.getRight()).getVariableName() : (b.getRight() instanceof LiteralNode ? String.valueOf(((LiteralNode) b.getRight()).getRawValue()) : "...");
        return l + " " + b.getOperator() + " " + r;
    }
}
