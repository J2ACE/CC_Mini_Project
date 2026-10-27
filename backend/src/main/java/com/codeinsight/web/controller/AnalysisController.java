package com.codeinsight.web.controller;

import com.codeinsight.compiler.analyzer.StaticAnalysisRule;
import com.codeinsight.compiler.optimizer.OptimizationRule;
import com.codeinsight.compiler.service.AnalysisService;
import com.codeinsight.compiler.service.SampleCodeService;
import com.codeinsight.web.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AnalysisController {
    private final AnalysisService analysisService;
    private final SampleCodeService sampleCodeService;

    public AnalysisController(AnalysisService analysisService, SampleCodeService sampleCodeService) {
        this.analysisService = analysisService;
        this.sampleCodeService = sampleCodeService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResponseDto> analyze(@RequestBody AnalyzeRequestDto request) {
        String source = request != null && request.getSourceCode() != null ? request.getSourceCode() : "";
        AnalysisResponseDto response = analysisService.analyze(source);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/samples")
    public ResponseEntity<List<SampleProgramDto>> getSamples() {
        return ResponseEntity.ok(sampleCodeService.getSamples());
    }

    @GetMapping("/rules")
    public ResponseEntity<List<RuleDefinitionDto>> getRules() {
        List<RuleDefinitionDto> rules = new ArrayList<>();

        for (StaticAnalysisRule r : analysisService.getStaticAnalysisEngine().getRules()) {
            rules.add(new RuleDefinitionDto(r.getRuleId(), "STATIC_ANALYSIS", r.getName(), "Checks code quality and safety via AST/CFG."));
        }

        for (OptimizationRule r : analysisService.getOptimizationEngine().getRules()) {
            rules.add(new RuleDefinitionDto(r.getRuleId(), "OPTIMIZATION", r.getName(), "Classical compiler optimization pass."));
        }

        return ResponseEntity.ok(rules);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new LinkedHashMap<>();
        status.put("status", "UP");
        status.put("engine", "CodeInsight Compiler Engine v1.0.0");
        status.put("javaVersion", System.getProperty("java.version"));
        return ResponseEntity.ok(status);
    }
}
