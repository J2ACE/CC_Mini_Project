package com.codeinsight.compiler.analyzer;

import com.codeinsight.compiler.ast.node.ASTNode;
import com.codeinsight.compiler.ast.node.ProgramNode;
import com.codeinsight.compiler.cfg.BasicBlock;
import com.codeinsight.compiler.cfg.ControlFlowGraph;
import com.codeinsight.compiler.diagnostic.Category;
import com.codeinsight.compiler.diagnostic.Diagnostic;
import com.codeinsight.compiler.diagnostic.Severity;
import com.codeinsight.compiler.symbol.SymbolTable;

import java.util.ArrayList;
import java.util.List;

public class UnreachableCodeDetector implements StaticAnalysisRule {

    @Override
    public String getRuleId() {
        return "STA003";
    }

    @Override
    public String getName() {
        return "Unreachable Code Detection";
    }

    @Override
    public List<Diagnostic> analyze(ProgramNode ast, SymbolTable symbolTable, List<ControlFlowGraph> cfgs) {
        List<Diagnostic> diagnostics = new ArrayList<>();

        for (ControlFlowGraph cfg : cfgs) {
            for (BasicBlock block : cfg.getBlocks()) {
                if (block.isUnreachable() && !block.getStatements().isEmpty()) {
                    ASTNode firstStmt = block.getStatements().get(0);
                    diagnostics.add(new Diagnostic(
                            "STA003",
                            Category.STATIC_ANALYSIS,
                            Severity.WARNING,
                            "Unreachable code detected in function '" + cfg.getFunctionName() + "'",
                            "This basic block cannot be reached along any control-flow path from the function entry.",
                            "Remove unreachable statements or adjust upstream branching conditions.",
                            firstStmt.getPosition().getLine(),
                            firstStmt.getPosition().getColumn(),
                            firstStmt.getPosition().getLength(),
                            "Unreachable block " + block.getId(),
                            "",
                            "SAFE"
                    ));
                }
            }
        }

        return diagnostics;
    }
}
