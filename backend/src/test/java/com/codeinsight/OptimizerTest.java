package com.codeinsight;

import com.codeinsight.compiler.service.AnalysisService;
import com.codeinsight.web.dto.AnalysisResponseDto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OptimizerTest {

    @Test
    public void testConstantFoldingOptimization() {
        String code = "int test() { int sec = 60 * 60; return sec; }";
        AnalysisService service = new AnalysisService();
        AnalysisResponseDto res = service.analyze(code);

        boolean found = res.getDiagnostics().stream()
                .anyMatch(d -> d.getRuleId().equals("OPT001") && d.getReplacementSnippet().equals("3600"));
        assertThat(found).isTrue();
    }

    @Test
    public void testStrengthReductionOptimization() {
        String code = "int test(int x) { int y = x * 2; return y; }";
        AnalysisService service = new AnalysisService();
        AnalysisResponseDto res = service.analyze(code);

        boolean found = res.getDiagnostics().stream()
                .anyMatch(d -> d.getRuleId().equals("OPT005") && d.getReplacementSnippet().contains("<< 1"));
        assertThat(found).isTrue();
    }

    @Test
    public void testAlgebraicSimplification() {
        String code = "int test(int x) { int y = x * 1; return y; }";
        AnalysisService service = new AnalysisService();
        AnalysisResponseDto res = service.analyze(code);

        boolean found = res.getDiagnostics().stream()
                .anyMatch(d -> d.getRuleId().equals("OPT003") && d.getReplacementSnippet().equals("x"));
        assertThat(found).isTrue();
    }
}
