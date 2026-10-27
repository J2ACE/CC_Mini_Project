package com.codeinsight;

import com.codeinsight.compiler.ast.node.SourcePosition;
import com.codeinsight.compiler.symbol.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SymbolTableTest {

    @Test
    public void testScopeNestingAndLookup() {
        SymbolTable table = new SymbolTable();
        Symbol globalX = new Symbol("x", Type.INT, SymbolKind.VARIABLE, new SourcePosition(1, 0, 0, 1));
        table.define(globalX);

        table.enterScope("Function_main", ScopeType.FUNCTION);
        Symbol localY = new Symbol("y", Type.FLOAT, SymbolKind.VARIABLE, new SourcePosition(2, 0, 0, 1));
        table.define(localY);

        assertThat(table.resolve("y")).isNotNull();
        assertThat(table.resolve("x")).isNotNull(); // Resolves in parent

        table.exitScope();
        assertThat(table.resolve("y")).isNull(); // Out of scope
        assertThat(table.resolve("x")).isNotNull();
    }
}
