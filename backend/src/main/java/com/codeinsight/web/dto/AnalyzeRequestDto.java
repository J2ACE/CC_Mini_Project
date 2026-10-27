package com.codeinsight.web.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AnalyzeRequestDto {
    private String sourceCode;

    public AnalyzeRequestDto() {}

    @JsonCreator
    public AnalyzeRequestDto(@JsonProperty("sourceCode") String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }
}
