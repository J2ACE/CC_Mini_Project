package com.codeinsight.compiler.optimizer;

import com.codeinsight.compiler.ast.node.ProgramNode;
import com.codeinsight.compiler.cfg.ControlFlowGraph;
import com.codeinsight.compiler.diagnostic.Diagnostic;
import com.codeinsight.compiler.symbol.SymbolTable;

import java.util.List;

public interface OptimizationRule {
    String getRuleId();
    String getName();
    List<Diagnostic> optimize(ProgramNode ast, SymbolTable symbolTable, List<ControlFlowGraph> cfgs);
}
