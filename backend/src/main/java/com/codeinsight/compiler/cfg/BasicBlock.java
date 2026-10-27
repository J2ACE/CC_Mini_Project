package com.codeinsight.compiler.cfg;

import com.codeinsight.compiler.ast.node.ASTNode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

public class BasicBlock {
    private final String id;
    private final List<ASTNode> statements = new ArrayList<>();
    private final List<CFGEdge> incomingEdges = new ArrayList<>();
    private final List<CFGEdge> outgoingEdges = new ArrayList<>();
    private boolean unreachable = false;

    // Data-flow sets
    @JsonIgnore
    private final Set<String> genReaching = new HashSet<>();
    @JsonIgnore
    private final Set<String> killReaching = new HashSet<>();
    @JsonIgnore
    private final Set<String> inReaching = new HashSet<>();
    @JsonIgnore
    private final Set<String> outReaching = new HashSet<>();

    @JsonIgnore
    private final Set<String> genLive = new HashSet<>();
    @JsonIgnore
    private final Set<String> killLive = new HashSet<>();
    @JsonIgnore
    private final Set<String> inLive = new HashSet<>();
    @JsonIgnore
    private final Set<String> outLive = new HashSet<>();

    @JsonIgnore
    private final Set<String> genAvail = new HashSet<>();
    @JsonIgnore
    private final Set<String> killAvail = new HashSet<>();
    @JsonIgnore
    private final Set<String> inAvail = new HashSet<>();
    @JsonIgnore
    private final Set<String> outAvail = new HashSet<>();

    public BasicBlock(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public List<ASTNode> getStatements() {
        return statements;
    }

    public void addStatement(ASTNode node) {
        if (node != null) {
            statements.add(node);
        }
    }

    public List<CFGEdge> getIncomingEdges() {
        return incomingEdges;
    }

    public List<CFGEdge> getOutgoingEdges() {
        return outgoingEdges;
    }

    public void addOutgoingEdge(CFGEdge edge) {
        outgoingEdges.add(edge);
        edge.getTarget().incomingEdges.add(edge);
    }

    public boolean isUnreachable() {
        return unreachable;
    }

    public void setUnreachable(boolean unreachable) {
        this.unreachable = unreachable;
    }

    @JsonProperty("successors")
    public List<String> getSuccessorIds() {
        List<String> ids = new ArrayList<>();
        for (CFGEdge edge : outgoingEdges) {
            ids.add(edge.getTarget().getId());
        }
        return ids;
    }

    @JsonProperty("predecessors")
    public List<String> getPredecessorIds() {
        List<String> ids = new ArrayList<>();
        for (CFGEdge edge : incomingEdges) {
            ids.add(edge.getSource().getId());
        }
        return ids;
    }

    // Reaching Definitions getters
    public Set<String> getGenReaching() { return genReaching; }
    public Set<String> getKillReaching() { return killReaching; }
    public Set<String> getInReaching() { return inReaching; }
    public Set<String> getOutReaching() { return outReaching; }

    // Live Variables getters
    public Set<String> getGenLive() { return genLive; }
    public Set<String> getKillLive() { return killLive; }
    public Set<String> getInLive() { return inLive; }
    public Set<String> getOutLive() { return outLive; }

    // Available Expressions getters
    public Set<String> getGenAvail() { return genAvail; }
    public Set<String> getKillAvail() { return killAvail; }
    public Set<String> getInAvail() { return inAvail; }
    public Set<String> getOutAvail() { return outAvail; }
}
