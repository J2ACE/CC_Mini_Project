package com.codeinsight.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SampleProgramDto {
    private final String id;
    private final String name;
    private final String description;
    private final String sourceCode;

    public SampleProgramDto(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("sourceCode") String sourceCode) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.sourceCode = sourceCode;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getSourceCode() {
        return sourceCode;
    }
}
