package com.codeinsight.compiler.frontend;

import com.codeinsight.compiler.diagnostic.Category;
import com.codeinsight.compiler.diagnostic.Diagnostic;
import com.codeinsight.compiler.diagnostic.DiagnosticCollector;
import com.codeinsight.compiler.diagnostic.Severity;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

public class SyntaxErrorListener extends BaseErrorListener {
    private final DiagnosticCollector collector;

    public SyntaxErrorListener(DiagnosticCollector collector) {
        this.collector = collector;
    }

    @Override
    public void syntaxError(
            Recognizer<?, ?> recognizer,
            Object offendingSymbol,
            int line,
            int charPositionInLine,
            String msg,
            RecognitionException e) {

        int length = 1;
        String snippet = "";
        if (offendingSymbol instanceof Token) {
            Token token = (Token) offendingSymbol;
            snippet = token.getText() != null ? token.getText() : "";
            length = Math.max(1, snippet.length());
        }

        Diagnostic diagnostic = new Diagnostic(
                "SYN001",
                Category.SYNTAX,
                Severity.ERROR,
                "Syntax Error: " + msg,
                "The source code does not adhere to the MiniLang grammatical rules at this token.",
                "Verify statement syntax, matching delimiters, or proper punctuation.",
                line,
                charPositionInLine,
                length,
                snippet,
                "",
                "SAFE"
        );
        collector.add(diagnostic);
    }
}
