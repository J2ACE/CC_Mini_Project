package com.codeinsight;

import com.codeinsight.compiler.ast.builder.ASTBuilderVisitor;
import com.codeinsight.compiler.ast.node.FunctionNode;
import com.codeinsight.compiler.ast.node.ProgramNode;
import com.codeinsight.compiler.cfg.CFGBuilder;
import com.codeinsight.compiler.cfg.ControlFlowGraph;
import com.codeinsight.compiler.dataflow.LiveVariableAnalysis;
import com.codeinsight.compiler.dataflow.ReachingDefinitions;
import com.codeinsight.compiler.grammar.MiniLangLexer;
import com.codeinsight.compiler.grammar.MiniLangParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DataFlowTest {

    @Test
    public void testReachingDefinitionsAndLiveVariables() {
        String code = "int test(int x) { int a = 10; int b = a + x; return b; }";
        MiniLangLexer lexer = new MiniLangLexer(CharStreams.fromString(code));
        MiniLangParser parser = new MiniLangParser(new CommonTokenStream(lexer));
        ProgramNode prog = (ProgramNode) new ASTBuilderVisitor().visit(parser.program());
        FunctionNode fn = prog.getFunctions().get(0);

        ControlFlowGraph cfg = CFGBuilder.build(fn);
        ReachingDefinitions.compute(cfg);
        LiveVariableAnalysis.compute(cfg);

        assertThat(cfg.getBlocks()).isNotEmpty();
    }
}
