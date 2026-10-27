package com.codeinsight.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RuleDefinitionDto {
    private final String id;
    private final String category;
    private final String name;
    private final String description;

    public RuleDefinitionDto(
            @JsonProperty("id") String id,
            @JsonProperty("category") String category,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description) {
        this.id = id;
        this.category = category;
        this.name = name;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
