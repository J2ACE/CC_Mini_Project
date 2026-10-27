package com.codeinsight.compiler.optimizer;

import com.codeinsight.compiler.ast.node.ProgramNode;
import com.codeinsight.compiler.cfg.ControlFlowGraph;
import com.codeinsight.compiler.diagnostic.Diagnostic;
import com.codeinsight.compiler.symbol.SymbolTable;

import java.util.ArrayList;
import java.util.List;

public class OptimizationEngine {
    private final List<OptimizationRule> rules = new ArrayList<>();

    public OptimizationEngine() {
        rules.add(new ConstantFolder());
        rules.add(new ConstantPropagator());
        rules.add(new AlgebraicSimplifier());
        rules.add(new CommonSubexpressionEliminator());
        rules.add(new StrengthReducer());
        rules.add(new LoopInvariantMotion());
    }

    public List<Diagnostic> run(ProgramNode ast, SymbolTable symbolTable, List<ControlFlowGraph> cfgs) {
        List<Diagnostic> results = new ArrayList<>();
        for (OptimizationRule rule : rules) {
            results.addAll(rule.optimize(ast, symbolTable, cfgs));
        }
        return results;
    }

    public List<OptimizationRule> getRules() {
        return rules;
    }
}
