package com.codeinsight;

import com.codeinsight.compiler.diagnostic.DiagnosticCollector;
import com.codeinsight.compiler.frontend.SyntaxErrorListener;
import com.codeinsight.compiler.grammar.MiniLangLexer;
import com.codeinsight.compiler.grammar.MiniLangParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LexerParserTest {

    @Test
    public void testValidProgramParsing() {
        String code = "int main() { int x = 10; return x; }";
        DiagnosticCollector collector = new DiagnosticCollector();

        MiniLangLexer lexer = new MiniLangLexer(CharStreams.fromString(code));
        lexer.removeErrorListeners();
        lexer.addErrorListener(new SyntaxErrorListener(collector));

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MiniLangParser parser = new MiniLangParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new SyntaxErrorListener(collector));

        MiniLangParser.ProgramContext tree = parser.program();

        assertThat(collector.hasErrors()).isFalse();
        assertThat(tree).isNotNull();
    }

    @Test
    public void testSyntaxErrorDetection() {
        String invalidCode = "int main() { int x = ; return; }"; // missing expression
        DiagnosticCollector collector = new DiagnosticCollector();

        MiniLangLexer lexer = new MiniLangLexer(CharStreams.fromString(invalidCode));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MiniLangParser parser = new MiniLangParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new SyntaxErrorListener(collector));

        parser.program();

        assertThat(collector.hasErrors()).isTrue();
        assertThat(collector.getDiagnostics().get(0).getRuleId()).isEqualTo("SYN001");
    }
}
