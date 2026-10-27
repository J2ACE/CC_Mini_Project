package com.codeinsight.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TransformationDto {
    private final String type;
    private final int line;
    private final String description;

    public TransformationDto(
            @JsonProperty("type") String type,
            @JsonProperty("line") int line,
            @JsonProperty("description") String description) {
        this.type = type;
        this.line = line;
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public int getLine() {
        return line;
    }

    public String getDescription() {
        return description;
    }
}
