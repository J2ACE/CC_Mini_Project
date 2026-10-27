package com.codeinsight;

import com.codeinsight.compiler.service.AnalysisService;
import com.codeinsight.compiler.cfg.ControlFlowGraph;
import com.codeinsight.web.dto.AnalysisResponseDto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CFGBuilderTest {

    @Test
    public void testBranchingCFGGeneration() {
        String code = "int max(int a, int b) { if (a > b) { return a; } else { return b; } }";
        AnalysisService service = new AnalysisService();
        AnalysisResponseDto res = service.analyze(code);

        assertThat(res.getControlFlowGraphs()).hasSize(1);
        ControlFlowGraph cfg = res.getControlFlowGraphs().get(0);
        assertThat(cfg.getFunctionName()).isEqualTo("max");
        assertThat(cfg.getBlocks().size()).isGreaterThanOrEqualTo(4);
        assertThat(cfg.getDotRepresentation()).contains("digraph CFG_max");
    }
}
