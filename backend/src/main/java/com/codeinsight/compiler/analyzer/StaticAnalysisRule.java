package com.codeinsight.compiler.analyzer;

import com.codeinsight.compiler.ast.node.ProgramNode;
import com.codeinsight.compiler.cfg.ControlFlowGraph;
import com.codeinsight.compiler.diagnostic.Diagnostic;
import com.codeinsight.compiler.symbol.SymbolTable;

import java.util.List;

public interface StaticAnalysisRule {
    String getRuleId();
    String getName();
    List<Diagnostic> analyze(ProgramNode ast, SymbolTable symbolTable, List<ControlFlowGraph> cfgs);
}
