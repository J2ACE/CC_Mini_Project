package com.codeinsight.compiler.ast.node;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SourcePosition {
    private final int line;
    private final int column;
    private final int startIndex;
    private final int stopIndex;
    private final int length;

    public SourcePosition(
            @JsonProperty("line") int line,
            @JsonProperty("column") int column,
            @JsonProperty("startIndex") int startIndex,
            @JsonProperty("stopIndex") int stopIndex) {
        this.line = line;
        this.column = column;
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.length = Math.max(1, (stopIndex - startIndex) + 1);
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getStopIndex() {
        return stopIndex;
    }

    public int getLength() {
        return length;
    }

    @Override
    public String toString() {
        return line + ":" + column;
    }
}
