package com.codeinsight.compiler.analyzer;

import com.codeinsight.compiler.ast.node.ProgramNode;
import com.codeinsight.compiler.cfg.ControlFlowGraph;
import com.codeinsight.compiler.diagnostic.Category;
import com.codeinsight.compiler.diagnostic.Diagnostic;
import com.codeinsight.compiler.diagnostic.Severity;
import com.codeinsight.compiler.symbol.Scope;
import com.codeinsight.compiler.symbol.Symbol;
import com.codeinsight.compiler.symbol.SymbolKind;
import com.codeinsight.compiler.symbol.SymbolTable;

import java.util.ArrayList;
import java.util.List;

public class UnusedVariableDetector implements StaticAnalysisRule {

    @Override
    public String getRuleId() {
        return "STA001";
    }

    @Override
    public String getName() {
        return "Unused Variable Detection";
    }

    @Override
    public List<Diagnostic> analyze(ProgramNode ast, SymbolTable symbolTable, List<ControlFlowGraph> cfgs) {
        List<Diagnostic> diagnostics = new ArrayList<>();
        checkScope(symbolTable.getRootScope(), diagnostics);
        return diagnostics;
    }

    private void checkScope(Scope scope, List<Diagnostic> diagnostics) {
        for (Symbol symbol : scope.getSymbolsList()) {
            if (symbol.getKind() == SymbolKind.VARIABLE && symbol.getReadCount() == 0) {
                diagnostics.add(new Diagnostic(
                        "STA001",
                        Category.STATIC_ANALYSIS,
                        Severity.WARNING,
                        "Unused variable '" + symbol.getName() + "'",
                        "Variable '" + symbol.getName() + "' is declared but its value is never read in any accessible execution path.",
                        "Remove the unused variable declaration or use it in subsequent calculations.",
                        symbol.getDeclarationPosition().getLine(),
                        symbol.getDeclarationPosition().getColumn(),
                        symbol.getName().length(),
                        symbol.getName(),
                        "",
                        "SAFE"
                ));
            }
        }

        for (Scope child : scope.getChildren()) {
            checkScope(child, diagnostics);
        }
    }
}
