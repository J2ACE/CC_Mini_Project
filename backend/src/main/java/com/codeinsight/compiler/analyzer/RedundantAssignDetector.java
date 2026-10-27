package com.codeinsight.compiler.analyzer;

import com.codeinsight.compiler.ast.node.*;
import com.codeinsight.compiler.cfg.BasicBlock;
import com.codeinsight.compiler.cfg.ControlFlowGraph;
import com.codeinsight.compiler.diagnostic.Category;
import com.codeinsight.compiler.diagnostic.Diagnostic;
import com.codeinsight.compiler.diagnostic.Severity;
import com.codeinsight.compiler.symbol.SymbolTable;

import java.util.ArrayList;
import java.util.List;

public class RedundantAssignDetector implements StaticAnalysisRule {

    @Override
    public String getRuleId() {
        return "STA004";
    }

    @Override
    public String getName() {
        return "Redundant / Dead Assignment Detection";
    }

    @Override
    public List<Diagnostic> analyze(ProgramNode ast, SymbolTable symbolTable, List<ControlFlowGraph> cfgs) {
        List<Diagnostic> diagnostics = new ArrayList<>();

        for (ControlFlowGraph cfg : cfgs) {
            for (BasicBlock block : cfg.getBlocks()) {
                if (block.isUnreachable()) continue;

                List<ASTNode> stmts = block.getStatements();
                for (int i = 0; i < stmts.size(); i++) {
                    ASTNode current = stmts.get(i);
                    String assignedVar = null;
                    SourcePosition pos = current.getPosition();

                    if (current instanceof AssignNode) {
                        assignedVar = ((AssignNode) current).getTargetVar();
                    } else if (current instanceof VarDeclNode) {
                        VarDeclNode vd = (VarDeclNode) current;
                        if (vd.getInitializer() != null) {
                            assignedVar = vd.getName();
                        }
                    }

                    if (assignedVar != null) {
                        // Check if the variable is immediately reassigned before being read
                        boolean readFound = false;
                        boolean overwritten = false;

                        for (int j = i + 1; j < stmts.size(); j++) {
                            ASTNode next = stmts.get(j);
                            if (containsRead(next, assignedVar)) {
                                readFound = true;
                                break;
                            }
                            if (isAssigned(next, assignedVar)) {
                                overwritten = true;
                                break;
                            }
                        }

                        if (!readFound && overwritten) {
                            diagnostics.add(new Diagnostic(
                                    "STA004",
                                    Category.STATIC_ANALYSIS,
                                    Severity.WARNING,
                                    "Redundant assignment to '" + assignedVar + "'",
                                    "Variable '" + assignedVar + "' is assigned a value that is overwritten before being read.",
                                    "Remove the dead assignment or update variable usage.",
                                    pos.getLine(),
                                    pos.getColumn(),
                                    assignedVar.length(),
                                    assignedVar + " = ...",
                                    "",
                                    "SAFE"
                            ));
                        }
                    }
                }
            }
        }

        return diagnostics;
    }

    private boolean containsRead(ASTNode node, String varName) {
        final boolean[] found = {false};
        node.accept(new com.codeinsight.compiler.ast.visitor.BaseASTVisitor<Void, Void>() {
            @Override
            public Void visit(VarRefNode ref, Void ctx) {
                if (ref.getVariableName().equals(varName)) {
                    found[0] = true;
                }
                return null;
            }
        }, null);
        return found[0];
    }

    private boolean isAssigned(ASTNode node, String varName) {
        if (node instanceof AssignNode) {
            return ((AssignNode) node).getTargetVar().equals(varName);
        }
        return false;
    }
}
