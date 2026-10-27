package com.codeinsight.compiler.cfg;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CFGEdge {
    @JsonIgnore
    private final BasicBlock source;
    @JsonIgnore
    private final BasicBlock target;
    private final EdgeType type;

    public CFGEdge(BasicBlock source, BasicBlock target, EdgeType type) {
        this.source = source;
        this.target = target;
        this.type = type;
    }

    public BasicBlock getSource() {
        return source;
    }

    public BasicBlock getTarget() {
        return target;
    }

    @JsonProperty("type")
    public EdgeType getType() {
        return type;
    }

    @JsonProperty("sourceId")
    public String getSourceId() {
        return source != null ? source.getId() : "";
    }

    @JsonProperty("targetId")
    public String getTargetId() {
        return target != null ? target.getId() : "";
    }
}
