package com.codeinsight;

import com.codeinsight.compiler.ast.builder.ASTBuilderVisitor;
import com.codeinsight.compiler.ast.node.*;
import com.codeinsight.compiler.grammar.MiniLangLexer;
import com.codeinsight.compiler.grammar.MiniLangParser;
import com.codeinsight.compiler.symbol.Type;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ASTBuilderTest {

    @Test
    public void testASTConstruction() {
        String code = "int foo(int a) { return a + 5; }";
        MiniLangLexer lexer = new MiniLangLexer(CharStreams.fromString(code));
        MiniLangParser parser = new MiniLangParser(new CommonTokenStream(lexer));

        ASTBuilderVisitor visitor = new ASTBuilderVisitor();
        ProgramNode program = (ProgramNode) visitor.visit(parser.program());

        assertThat(program.getFunctions()).hasSize(1);
        FunctionNode fn = program.getFunctions().get(0);
        assertThat(fn.getName()).isEqualTo("foo");
        assertThat(fn.getReturnType()).isEqualTo(Type.INT);
        assertThat(fn.getParameters()).hasSize(1);
        assertThat(fn.getBody().getStatements()).hasSize(1);

        ReturnNode ret = (ReturnNode) fn.getBody().getStatements().get(0);
        assertThat(ret.getValue()).isInstanceOf(BinaryExpr.class);
        BinaryExpr bin = (BinaryExpr) ret.getValue();
        assertThat(bin.getOperator()).isEqualTo("+");
        assertThat(bin.getPosition().getLine()).isEqualTo(1);
    }
}
