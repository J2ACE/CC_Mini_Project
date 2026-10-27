package com.codeinsight.compiler.analyzer;

import com.codeinsight.compiler.ast.node.*;
import com.codeinsight.compiler.ast.visitor.BaseASTVisitor;
import com.codeinsight.compiler.cfg.BasicBlock;
import com.codeinsight.compiler.cfg.ControlFlowGraph;
import com.codeinsight.compiler.diagnostic.Category;
import com.codeinsight.compiler.diagnostic.Diagnostic;
import com.codeinsight.compiler.diagnostic.Severity;
import com.codeinsight.compiler.symbol.Symbol;
import com.codeinsight.compiler.symbol.SymbolTable;

import java.util.*;

public class UninitializedReadDetector implements StaticAnalysisRule {

    @Override
    public String getRuleId() {
        return "STA002";
    }

    @Override
    public String getName() {
        return "Uninitialized Variable Read Detection";
    }

    @Override
    public List<Diagnostic> analyze(ProgramNode ast, SymbolTable symbolTable, List<ControlFlowGraph> cfgs) {
        List<Diagnostic> diagnostics = new ArrayList<>();

        for (ControlFlowGraph cfg : cfgs) {
            for (BasicBlock block : cfg.getBlocks()) {
                if (block.isUnreachable()) continue;

                Set<String> currentlyDefined = new HashSet<>();
                // From reaching definitions at entry of block
                for (String def : block.getInReaching()) {
                    int atIdx = def.indexOf('@');
                    if (atIdx > 0) currentlyDefined.add(def.substring(0, atIdx));
                }

                for (ASTNode node : block.getStatements()) {
                    if (node instanceof VarDeclNode) {
                        VarDeclNode vd = (VarDeclNode) node;
                        if (vd.getInitializer() != null) {
                            checkReads(vd.getInitializer(), currentlyDefined, symbolTable, diagnostics);
                            currentlyDefined.add(vd.getName());
                        }
                    } else if (node instanceof AssignNode) {
                        AssignNode an = (AssignNode) node;
                        checkReads(an.getValue(), currentlyDefined, symbolTable, diagnostics);
                        currentlyDefined.add(an.getTargetVar());
                    } else {
                        checkReads(node, currentlyDefined, symbolTable, diagnostics);
                    }
                }
            }
        }

        return diagnostics;
    }

    private void checkReads(ASTNode node, Set<String> defined, SymbolTable symbolTable, List<Diagnostic> diagnostics) {
        if (node == null) return;
        node.accept(new BaseASTVisitor<Void, Void>() {
            @Override
            public Void visit(VarRefNode ref, Void ctx) {
                Symbol s = symbolTable.resolve(ref.getVariableName());
                if (s != null && !defined.contains(ref.getVariableName()) && !s.isInitialized()) {
                    diagnostics.add(new Diagnostic(
                            "STA002",
                            Category.STATIC_ANALYSIS,
                            Severity.ERROR,
                            "Read of uninitialized variable '" + ref.getVariableName() + "'",
                            "Variable '" + ref.getVariableName() + "' is read along an execution path before being assigned any value.",
                            "Initialize '" + ref.getVariableName() + "' prior to read.",
                            ref.getPosition().getLine(),
                            ref.getPosition().getColumn(),
                            ref.getVariableName().length(),
                            ref.getVariableName(),
                            "",
                            "SAFE"
                    ));
                }
                return null;
            }
        }, null);
    }
}
