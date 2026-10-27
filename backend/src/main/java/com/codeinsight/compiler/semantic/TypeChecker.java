package com.codeinsight.compiler.semantic;

import com.codeinsight.compiler.symbol.Type;

public class TypeChecker {

    public static boolean isAssignable(Type target, Type source) {
        if (target == Type.UNKNOWN || source == Type.UNKNOWN) return true;
        if (target == source) return true;
        // int can be implicitly widened to float
        if (target == Type.FLOAT && source == Type.INT) return true;
        return false;
    }

    public static Type getBinaryResultType(String op, Type left, Type right) {
        if (left == Type.UNKNOWN || right == Type.UNKNOWN) return Type.UNKNOWN;

        switch (op) {
            case "+":
            case "-":
            case "*":
            case "/":
            case "%":
                if (left.isNumeric() && right.isNumeric()) {
                    if (left == Type.FLOAT || right == Type.FLOAT) {
                        return (op.equals("%")) ? Type.UNKNOWN : Type.FLOAT;
                    }
                    return Type.INT;
                }
                return Type.UNKNOWN;

            case "<":
            case "<=":
            case ">":
            case ">=":
                if (left.isNumeric() && right.isNumeric()) return Type.BOOL;
                if (left == Type.CHAR && right == Type.CHAR) return Type.BOOL;
                return Type.UNKNOWN;

            case "==":
            case "!=":
                if (left == right) return Type.BOOL;
                if (left.isNumeric() && right.isNumeric()) return Type.BOOL;
                return Type.UNKNOWN;

            case "&&":
            case "||":
                if (left == Type.BOOL && right == Type.BOOL) return Type.BOOL;
                return Type.UNKNOWN;

            default:
                return Type.UNKNOWN;
        }
    }

    public static Type getUnaryResultType(String op, Type operand) {
        if (operand == Type.UNKNOWN) return Type.UNKNOWN;
        switch (op) {
            case "!":
                return operand == Type.BOOL ? Type.BOOL : Type.UNKNOWN;
            case "+":
            case "-":
                return operand.isNumeric() ? operand : Type.UNKNOWN;
            default:
                return Type.UNKNOWN;
        }
    }
}
