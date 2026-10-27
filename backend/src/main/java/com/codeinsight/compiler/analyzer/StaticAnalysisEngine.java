package com.codeinsight.compiler.analyzer;

import com.codeinsight.compiler.ast.node.ProgramNode;
import com.codeinsight.compiler.cfg.ControlFlowGraph;
import com.codeinsight.compiler.diagnostic.Diagnostic;
import com.codeinsight.compiler.symbol.SymbolTable;

import java.util.ArrayList;
import java.util.List;

public class StaticAnalysisEngine {
    private final List<StaticAnalysisRule> rules = new ArrayList<>();

    public StaticAnalysisEngine() {
        rules.add(new UnusedVariableDetector());
        rules.add(new UninitializedReadDetector());
        rules.add(new UnreachableCodeDetector());
        rules.add(new RedundantAssignDetector());
        rules.add(new TautologicalConditionDetector());
    }

    public List<Diagnostic> run(ProgramNode ast, SymbolTable symbolTable, List<ControlFlowGraph> cfgs) {
        List<Diagnostic> results = new ArrayList<>();
        for (StaticAnalysisRule rule : rules) {
            results.addAll(rule.analyze(ast, symbolTable, cfgs));
        }
        return results;
    }

    public List<StaticAnalysisRule> getRules() {
        return rules;
    }
}
