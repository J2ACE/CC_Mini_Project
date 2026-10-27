package com.codeinsight.compiler.cfg;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class ControlFlowGraph {
    private final String functionName;
    private final BasicBlock entryBlock;
    private final BasicBlock exitBlock;
    private final List<BasicBlock> blocks = new ArrayList<>();
    private String dotRepresentation = "";

    public ControlFlowGraph(String functionName, BasicBlock entryBlock, BasicBlock exitBlock) {
        this.functionName = functionName;
        this.entryBlock = entryBlock;
        this.exitBlock = exitBlock;
    }

    @JsonProperty("functionName")
    public String getFunctionName() {
        return functionName;
    }

    public BasicBlock getEntryBlock() {
        return entryBlock;
    }

    public BasicBlock getExitBlock() {
        return exitBlock;
    }

    @JsonProperty("blocks")
    public List<BasicBlock> getBlocks() {
        return blocks;
    }

    public void addBlock(BasicBlock block) {
        if (block != null && !blocks.contains(block)) {
            blocks.add(block);
        }
    }

    @JsonProperty("dotRepresentation")
    public String getDotRepresentation() {
        return dotRepresentation;
    }

    public void setDotRepresentation(String dotRepresentation) {
        this.dotRepresentation = dotRepresentation;
    }
}
