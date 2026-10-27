package com.codeinsight;

import com.codeinsight.compiler.service.AnalysisService;
import com.codeinsight.web.dto.AnalysisResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class IntegrationAnalysisTest {

    @Test
    public void testCompletePipelineIntegration() {
        String code = "int compute(int x, int y) {\n" +
                "    int base = 10 + 20 * 2;\n" +
                "    int adjusted = x * 1;\n" +
                "    int unused = 99;\n" +
                "    if (10 > 5) {\n" +
                "        return base + adjusted;\n" +
                "    } else {\n" +
                "        return 0;\n" +
                "    }\n" +
                "}";

        AnalysisService service = new AnalysisService();
        AnalysisResponseDto response = service.analyze(code);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getTokens()).isNotEmpty();
        assertThat(response.getAst()).isNotNull();
        assertThat(response.getSymbolTable()).isNotNull();
        assertThat(response.getControlFlowGraphs()).isNotEmpty();
        assertThat(response.getDiagnostics()).isNotEmpty();
        assertThat(response.getMetrics().get("totalTokens")).isNotNull();
    }
}
