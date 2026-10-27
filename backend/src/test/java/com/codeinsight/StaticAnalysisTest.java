package com.codeinsight;

import com.codeinsight.compiler.service.AnalysisService;
import com.codeinsight.web.dto.AnalysisResponseDto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StaticAnalysisTest {

    @Test
    public void testUnusedVariableDetection() {
        String code = "int test() { int unused = 100; return 0; }";
        AnalysisService service = new AnalysisService();
        AnalysisResponseDto res = service.analyze(code);

        boolean found = res.getDiagnostics().stream().anyMatch(d -> d.getRuleId().equals("STA001"));
        assertThat(found).isTrue();
    }

    @Test
    public void testTautologicalConditionDetection() {
        String code = "int test(int x) { if (10 > 5) { return x; } return 0; }";
        AnalysisService service = new AnalysisService();
        AnalysisResponseDto res = service.analyze(code);

        boolean found = res.getDiagnostics().stream().anyMatch(d -> d.getRuleId().equals("STA005"));
        assertThat(found).isTrue();
    }
}
