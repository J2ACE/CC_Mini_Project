package com.codeinsight.compiler.symbol;

import com.codeinsight.compiler.ast.node.SourcePosition;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class Symbol {
    private final String name;
    private final Type type;
    private final SymbolKind kind;
    private final SourcePosition declarationPosition;
    private boolean initialized;
    private int readCount;
    private int writeCount;
    private boolean shadowed;
    private final List<SourcePosition> references = new ArrayList<>();
    private final List<Type> parameterTypes = new ArrayList<>();

    public Symbol(String name, Type type, SymbolKind kind, SourcePosition declarationPosition) {
        this.name = name;
        this.type = type;
        this.kind = kind;
        this.declarationPosition = declarationPosition;
        this.initialized = false;
        this.readCount = 0;
        this.writeCount = 0;
        this.shadowed = false;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public SymbolKind getKind() {
        return kind;
    }

    public SourcePosition getDeclarationPosition() {
        return declarationPosition;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public int getReadCount() {
        return readCount;
    }

    public void incrementReadCount() {
        this.readCount++;
    }

    public int getWriteCount() {
        return writeCount;
    }

    public void incrementWriteCount() {
        this.writeCount++;
    }

    public boolean isShadowed() {
        return shadowed;
    }

    public void setShadowed(boolean shadowed) {
        this.shadowed = shadowed;
    }

    public List<SourcePosition> getReferences() {
        return references;
    }

    public void addReference(SourcePosition pos) {
        if (pos != null) {
            this.references.add(pos);
        }
    }

    public List<Type> getParameterTypes() {
        return parameterTypes;
    }

    public void addParameterType(Type paramType) {
        this.parameterTypes.add(paramType);
    }
}
