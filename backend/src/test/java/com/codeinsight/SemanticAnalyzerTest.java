package com.codeinsight;

import com.codeinsight.compiler.service.AnalysisService;
import com.codeinsight.compiler.diagnostic.Diagnostic;
import com.codeinsight.web.dto.AnalysisResponseDto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SemanticAnalyzerTest {

    @Test
    public void testUndeclaredVariableDetection() {
        String code = "int main() { x = 10; return 0; }";
        AnalysisService service = new AnalysisService();
        AnalysisResponseDto res = service.analyze(code);

        boolean hasUndeclared = res.getDiagnostics().stream()
                .anyMatch(d -> d.getRuleId().equals("SEM001"));
        assertThat(hasUndeclared).isTrue();
    }

    @Test
    public void testTypeMismatchDetection() {
        String code = "int main() { int a = true; return a; }";
        AnalysisService service = new AnalysisService();
        AnalysisResponseDto res = service.analyze(code);

        boolean hasMismatch = res.getDiagnostics().stream()
                .anyMatch(d -> d.getRuleId().equals("SEM004"));
        assertThat(hasMismatch).isTrue();
    }
}
