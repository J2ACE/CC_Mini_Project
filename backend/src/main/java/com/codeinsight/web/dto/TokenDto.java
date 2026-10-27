package com.codeinsight.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TokenDto {
    private final int index;
    private final String category;
    private final String type;
    private final String text;
    private final int line;
    private final int column;

    public TokenDto(
            @JsonProperty("index") int index,
            @JsonProperty("category") String category,
            @JsonProperty("type") String type,
            @JsonProperty("text") String text,
            @JsonProperty("line") int line,
            @JsonProperty("column") int column) {
        this.index = index;
        this.category = category;
        this.type = type;
        this.text = text;
        this.line = line;
        this.column = column;
    }

    public TokenDto(String type, String text, int line, int column) {
        this(0, "Other", type, text, line, column);
    }

    public int getIndex() {
        return index;
    }

    public String getCategory() {
        return category;
    }

    public String getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}

