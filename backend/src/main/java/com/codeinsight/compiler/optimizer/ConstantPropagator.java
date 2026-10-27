package com.codeinsight.compiler.optimizer;

import com.codeinsight.compiler.ast.node.*;
import com.codeinsight.compiler.ast.visitor.BaseASTVisitor;
import com.codeinsight.compiler.cfg.BasicBlock;
import com.codeinsight.compiler.cfg.ControlFlowGraph;
import com.codeinsight.compiler.diagnostic.Category;
import com.codeinsight.compiler.diagnostic.Diagnostic;
import com.codeinsight.compiler.diagnostic.Severity;
import com.codeinsight.compiler.symbol.SymbolTable;

import java.util.*;

public class ConstantPropagator implements OptimizationRule {

    @Override
    public String getRuleId() {
        return "OPT002";
    }

    @Override
    public String getName() {
        return "Constant Propagation";
    }

    @Override
    public List<Diagnostic> optimize(ProgramNode ast, SymbolTable symbolTable, List<ControlFlowGraph> cfgs) {
        List<Diagnostic> diagnostics = new ArrayList<>();

        for (ControlFlowGraph cfg : cfgs) {
            Map<String, String> knownConstants = new HashMap<>();

            for (BasicBlock block : cfg.getBlocks()) {
                if (block.isUnreachable()) continue;

                for (ASTNode node : block.getStatements()) {
                    if (node instanceof VarDeclNode) {
                        VarDeclNode vd = (VarDeclNode) node;
                        if (vd.getInitializer() instanceof LiteralNode) {
                            knownConstants.put(vd.getName(), String.valueOf(((LiteralNode) vd.getInitializer()).getRawValue()));
                        } else {
                            knownConstants.remove(vd.getName());
                            checkExpr(vd.getInitializer(), knownConstants, diagnostics);
                        }
                    } else if (node instanceof AssignNode) {
                        AssignNode an = (AssignNode) node;
                        if (an.getValue() instanceof LiteralNode) {
                            knownConstants.put(an.getTargetVar(), String.valueOf(((LiteralNode) an.getValue()).getRawValue()));
                        } else {
                            checkExpr(an.getValue(), knownConstants, diagnostics);
                            knownConstants.remove(an.getTargetVar());
                        }
                    } else {
                        checkExpr(node, knownConstants, diagnostics);
                    }
                }
            }
        }

        return diagnostics;
    }

    private void checkExpr(ASTNode node, Map<String, String> constants, List<Diagnostic> diagnostics) {
        if (node == null) return;
        node.accept(new BaseASTVisitor<Void, Void>() {
            @Override
            public Void visit(VarRefNode ref, Void ctx) {
                String constVal = constants.get(ref.getVariableName());
                if (constVal != null) {
                    diagnostics.add(new Diagnostic(
                            "OPT002",
                            Category.OPTIMIZATION,
                            Severity.OPTIMIZATION,
                            "Constant propagation opportunity: '" + ref.getVariableName() + "'",
                            "Variable '" + ref.getVariableName() + "' holds constant value '" + constVal + "' along this path.",
                            "Propagate constant value '" + constVal + "' in place of variable '" + ref.getVariableName() + "'.",
                            ref.getPosition().getLine(),
                            ref.getPosition().getColumn(),
                            ref.getVariableName().length(),
                            ref.getVariableName(),
                            constVal,
                            "SAFE"
                    ));
                }
                return null;
            }
        }, null);
    }
}
