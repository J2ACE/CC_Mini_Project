package com.codeinsight.web.dto;

import com.codeinsight.compiler.ast.node.ProgramNode;
import com.codeinsight.compiler.cfg.ControlFlowGraph;
import com.codeinsight.compiler.diagnostic.Diagnostic;
import com.codeinsight.compiler.symbol.Scope;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AnalysisResponseDto {
    private final boolean success;
    private final Map<String, Object> metrics;
    private final List<TokenDto> tokens;
    private final ProgramNode ast;
    private final Scope symbolTable;
    private final List<ControlFlowGraph> controlFlowGraphs;
    private final List<Diagnostic> diagnostics;
    private final String optimizedCode;
    private final List<TransformationDto> transformationsApplied;

    public AnalysisResponseDto(
            @JsonProperty("success") boolean success,
            @JsonProperty("metrics") Map<String, Object> metrics,
            @JsonProperty("tokens") List<TokenDto> tokens,
            @JsonProperty("ast") ProgramNode ast,
            @JsonProperty("symbolTable") Scope symbolTable,
            @JsonProperty("controlFlowGraphs") List<ControlFlowGraph> controlFlowGraphs,
            @JsonProperty("diagnostics") List<Diagnostic> diagnostics,
            @JsonProperty("optimizedCode") String optimizedCode,
            @JsonProperty("transformationsApplied") List<TransformationDto> transformationsApplied) {
        this.success = success;
        this.metrics = metrics;
        this.tokens = tokens;
        this.ast = ast;
        this.symbolTable = symbolTable;
        this.controlFlowGraphs = controlFlowGraphs;
        this.diagnostics = diagnostics;
        this.optimizedCode = optimizedCode != null ? optimizedCode : "";
        this.transformationsApplied = transformationsApplied != null ? transformationsApplied : Collections.emptyList();
    }

    public AnalysisResponseDto(
            boolean success,
            Map<String, Object> metrics,
            List<TokenDto> tokens,
            ProgramNode ast,
            Scope symbolTable,
            List<ControlFlowGraph> controlFlowGraphs,
            List<Diagnostic> diagnostics) {
        this(success, metrics, tokens, ast, symbolTable, controlFlowGraphs, diagnostics, "", Collections.emptyList());
    }

    public boolean isSuccess() {
        return success;
    }

    public Map<String, Object> getMetrics() {
        return metrics;
    }

    public List<TokenDto> getTokens() {
        return tokens;
    }

    public ProgramNode getAst() {
        return ast;
    }

    public Scope getSymbolTable() {
        return symbolTable;
    }

    public List<ControlFlowGraph> getControlFlowGraphs() {
        return controlFlowGraphs;
    }

    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }

    public String getOptimizedCode() {
        return optimizedCode;
    }

    public List<TransformationDto> getTransformationsApplied() {
        return transformationsApplied;
    }
}

