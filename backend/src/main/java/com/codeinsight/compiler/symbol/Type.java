package com.codeinsight.compiler.symbol;

public enum Type {
    INT,
    FLOAT,
    CHAR,
    BOOL,
    VOID,
    UNKNOWN;

    public static Type fromString(String typeStr) {
        if (typeStr == null) return UNKNOWN;
        switch (typeStr.trim()) {
            case "int": return INT;
            case "float": return FLOAT;
            case "char": return CHAR;
            case "bool": return BOOL;
            case "void": return VOID;
            default: return UNKNOWN;
        }
    }

    public boolean isNumeric() {
        return this == INT || this == FLOAT;
    }

    public boolean isCompatibleWith(Type target) {
        if (this == target) return true;
        // int can be implicitly widened to float in expressions
        if (this == INT && target == FLOAT) return true;
        return false;
    }
}
