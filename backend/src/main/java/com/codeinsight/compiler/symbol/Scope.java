package com.codeinsight.compiler.symbol;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Scope {
    private final String name;
    private final ScopeType type;
    private final int level;
    
    @JsonIgnore
    private final Scope parent;
    
    private final Map<String, Symbol> symbols = new LinkedHashMap<>();
    private final List<Scope> children = new ArrayList<>();

    public Scope(String name, ScopeType type, int level, Scope parent) {
        this.name = name;
        this.type = type;
        this.level = level;
        this.parent = parent;
        if (parent != null) {
            parent.children.add(this);
        }
    }

    public String getName() {
        return name;
    }

    public ScopeType getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }

    public Scope getParent() {
        return parent;
    }

    @JsonProperty("symbols")
    public Collection<Symbol> getSymbolsList() {
        return symbols.values();
    }

    @JsonIgnore
    public Map<String, Symbol> getSymbolsMap() {
        return symbols;
    }

    public List<Scope> getChildren() {
        return children;
    }

    public boolean define(Symbol symbol) {
        if (symbols.containsKey(symbol.getName())) {
            return false;
        }
        symbols.put(symbol.getName(), symbol);
        return true;
    }

    public Symbol resolve(String name) {
        Symbol s = symbols.get(name);
        if (s != null) return s;
        if (parent != null) return parent.resolve(name);
        return null;
    }

    public Symbol resolveLocally(String name) {
        return symbols.get(name);
    }
}
