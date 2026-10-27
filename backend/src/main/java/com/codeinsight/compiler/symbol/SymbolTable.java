package com.codeinsight.compiler.symbol;

public class SymbolTable {
    private final Scope rootScope;
    private Scope currentScope;

    public SymbolTable() {
        this.rootScope = new Scope("Global", ScopeType.GLOBAL, 0, null);
        this.currentScope = rootScope;
    }

    public Scope getRootScope() {
        return rootScope;
    }

    public Scope getCurrentScope() {
        return currentScope;
    }

    public Scope enterScope(String name, ScopeType type) {
        currentScope = new Scope(name, type, currentScope.getLevel() + 1, currentScope);
        return currentScope;
    }

    public Scope exitScope() {
        if (currentScope.getParent() != null) {
            currentScope = currentScope.getParent();
        }
        return currentScope;
    }

    public boolean define(Symbol symbol) {
        return currentScope.define(symbol);
    }

    public Symbol resolve(String name) {
        return currentScope.resolve(name);
    }

    public Symbol resolveLocally(String name) {
        return currentScope.resolveLocally(name);
    }
}
