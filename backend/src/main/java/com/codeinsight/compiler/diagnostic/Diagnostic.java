package com.codeinsight.compiler.diagnostic;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Diagnostic {
    private final String ruleId;
    private final Category category;
    private final Severity severity;
    private final String message;
    private final String explanation;
    private final String suggestion;
    private final int line;
    private final int column;
    private final int length;
    private final String sourceSnippet;
    private final String replacementSnippet;
    private final String safetyLevel;

    public Diagnostic(
            @JsonProperty("ruleId") String ruleId,
            @JsonProperty("category") Category category,
            @JsonProperty("severity") Severity severity,
            @JsonProperty("message") String message,
            @JsonProperty("explanation") String explanation,
            @JsonProperty("suggestion") String suggestion,
            @JsonProperty("line") int line,
            @JsonProperty("column") int column,
            @JsonProperty("length") int length,
            @JsonProperty("sourceSnippet") String sourceSnippet,
            @JsonProperty("replacementSnippet") String replacementSnippet,
            @JsonProperty("safetyLevel") String safetyLevel) {
        this.ruleId = ruleId;
        this.category = category;
        this.severity = severity;
        this.message = message;
        this.explanation = explanation;
        this.suggestion = suggestion;
        this.line = line;
        this.column = column;
        this.length = length;
        this.sourceSnippet = sourceSnippet;
        this.replacementSnippet = replacementSnippet;
        this.safetyLevel = safetyLevel;
    }

    public String getRuleId() {
        return ruleId;
    }

    public Category getCategory() {
        return category;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getMessage() {
        return message;
    }

    public String getExplanation() {
        return explanation;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public int getLength() {
        return length;
    }

    public String getSourceSnippet() {
        return sourceSnippet;
    }

    public String getReplacementSnippet() {
        return replacementSnippet;
    }

    public String getSafetyLevel() {
        return safetyLevel;
    }
}
