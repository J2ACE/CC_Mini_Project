package com.codeinsight.compiler.semantic;

import com.codeinsight.compiler.ast.node.*;
import com.codeinsight.compiler.ast.visitor.BaseASTVisitor;
import com.codeinsight.compiler.diagnostic.Category;
import com.codeinsight.compiler.diagnostic.Diagnostic;
import com.codeinsight.compiler.diagnostic.DiagnosticCollector;
import com.codeinsight.compiler.diagnostic.Severity;
import com.codeinsight.compiler.symbol.Scope;
import com.codeinsight.compiler.symbol.Symbol;
import com.codeinsight.compiler.symbol.SymbolKind;
import com.codeinsight.compiler.symbol.SymbolTable;
import com.codeinsight.compiler.symbol.Type;

import java.util.ArrayList;
import java.util.List;

public class SemanticAnalyzerVisitor extends BaseASTVisitor<Type, Void> {
    private final SymbolTable symbolTable;
    private final DiagnosticCollector collector;
    private Scope currentScope;
    private FunctionNode currentFunction;

    public SemanticAnalyzerVisitor(SymbolTable symbolTable, DiagnosticCollector collector) {
        this.symbolTable = symbolTable;
        this.collector = collector;
        this.currentScope = symbolTable.getRootScope();
    }

    private void enterScopeByName(String name) {
        for (Scope child : currentScope.getChildren()) {
            if (child.getName().equals(name)) {
                currentScope = child;
                return;
            }
        }
    }

    private void exitScope() {
        if (currentScope.getParent() != null) {
            currentScope = currentScope.getParent();
        }
    }

    @Override
    public Type visit(ProgramNode node, Void context) {
        for (VarDeclNode global : node.getGlobals()) {
            global.accept(this, context);
        }
        for (FunctionNode fn : node.getFunctions()) {
            fn.accept(this, context);
        }
        return Type.VOID;
    }

    @Override
    public Type visit(FunctionNode node, Void context) {
        this.currentFunction = node;
        enterScopeByName("Function_" + node.getName());

        if (node.getBody() != null) {
            for (StatementNode stmt : node.getBody().getStatements()) {
                stmt.accept(this, context);
            }

            // Check non-void return path
            if (node.getReturnType() != Type.VOID) {
                if (!hasGuaranteedReturn(node.getBody())) {
                    collector.add(new Diagnostic(
                            "SEM010",
                            Category.SEMANTIC,
                            Severity.ERROR,
                            "Missing return statement in function '" + node.getName() + "'",
                            "Function '" + node.getName() + "' has non-void return type '" + node.getReturnType() + "', but not all execution paths return a value.",
                            "Add a return statement returning a value of type " + node.getReturnType() + ".",
                            node.getPosition().getLine(),
                            node.getPosition().getColumn(),
                            node.getName().length(),
                            node.getName(),
                            "",
                            "SAFE"
                    ));
                }
            }
        }

        exitScope();
        this.currentFunction = null;
        return Type.VOID;
    }

    private boolean hasGuaranteedReturn(StatementNode stmt) {
        if (stmt == null) return false;
        if (stmt instanceof ReturnNode) return true;
        if (stmt instanceof BlockNode) {
            BlockNode block = (BlockNode) stmt;
            for (StatementNode s : block.getStatements()) {
                if (hasGuaranteedReturn(s)) return true;
            }
            return false;
        }
        if (stmt instanceof IfNode) {
            IfNode ifNode = (IfNode) stmt;
            return ifNode.getElseBranch() != null &&
                    hasGuaranteedReturn(ifNode.getThenBranch()) &&
                    hasGuaranteedReturn(ifNode.getElseBranch());
        }
        return false;
    }

    @Override
    public Type visit(BlockNode node, Void context) {
        // Find matching child block scope
        int childIndex = 0;
        for (Scope child : currentScope.getChildren()) {
            if (child.getName().startsWith("Block_")) {
                currentScope = child;
                break;
            }
        }
        for (StatementNode stmt : node.getStatements()) {
            stmt.accept(this, context);
        }
        exitScope();
        return Type.VOID;
    }

    @Override
    public Type visit(VarDeclNode node, Void context) {
        if (node.getInitializer() != null) {
            Type initType = node.getInitializer().accept(this, context);
            if (!TypeChecker.isAssignable(node.getType(), initType)) {
                collector.add(new Diagnostic(
                        "SEM004",
                        Category.SEMANTIC,
                        Severity.ERROR,
                        "Type mismatch in variable declaration: '" + node.getName() + "'",
                        "Cannot assign expression of type '" + initType + "' to variable '" + node.getName() + "' of type '" + node.getType() + "'.",
                        "Ensure the assigned value matches the declared variable type.",
                        node.getPosition().getLine(),
                        node.getPosition().getColumn(),
                        node.getName().length(),
                        node.getName(),
                        "",
                        "SAFE"
                ));
            }
        }
        return Type.VOID;
    }

    @Override
    public Type visit(AssignNode node, Void context) {
        Symbol symbol = currentScope.resolve(node.getTargetVar());
        if (symbol == null) {
            collector.add(new Diagnostic(
                    "SEM001",
                    Category.SEMANTIC,
                    Severity.ERROR,
                    "Undeclared variable in assignment: '" + node.getTargetVar() + "'",
                    "Variable '" + node.getTargetVar() + "' has not been declared in any accessible scope.",
                    "Declare the variable with a data type before assigning to it.",
                    node.getPosition().getLine(),
                    node.getPosition().getColumn(),
                    node.getTargetVar().length(),
                    node.getTargetVar(),
                    "",
                    "SAFE"
            ));
        }

        Type valType = node.getValue() != null ? node.getValue().accept(this, context) : Type.UNKNOWN;

        if (symbol != null && !TypeChecker.isAssignable(symbol.getType(), valType)) {
            collector.add(new Diagnostic(
                    "SEM004",
                    Category.SEMANTIC,
                    Severity.ERROR,
                    "Type mismatch in assignment to '" + node.getTargetVar() + "'",
                    "Cannot assign value of type '" + valType + "' to variable '" + node.getTargetVar() + "' of type '" + symbol.getType() + "'.",
                    "Cast or convert value to " + symbol.getType() + ".",
                    node.getPosition().getLine(),
                    node.getPosition().getColumn(),
                    node.getTargetVar().length(),
                    node.getTargetVar(),
                    "",
                    "SAFE"
            ));
        }
        return Type.VOID;
    }

    @Override
    public Type visit(IfNode node, Void context) {
        Type condType = node.getCondition().accept(this, context);
        if (condType != Type.BOOL && condType != Type.UNKNOWN) {
            collector.add(new Diagnostic(
                    "SEM005",
                    Category.SEMANTIC,
                    Severity.ERROR,
                    "Non-boolean condition in 'if' statement",
                    "Condition expression evaluated to type '" + condType + "', but 'if' requires a boolean condition.",
                    "Use a relational or boolean expression.",
                    node.getPosition().getLine(),
                    node.getPosition().getColumn(),
                    2,
                    "if",
                    "",
                    "SAFE"
            ));
        }
        if (node.getThenBranch() != null) node.getThenBranch().accept(this, context);
        if (node.getElseBranch() != null) node.getElseBranch().accept(this, context);
        return Type.VOID;
    }

    @Override
    public Type visit(WhileNode node, Void context) {
        Type condType = node.getCondition().accept(this, context);
        if (condType != Type.BOOL && condType != Type.UNKNOWN) {
            collector.add(new Diagnostic(
                    "SEM005",
                    Category.SEMANTIC,
                    Severity.ERROR,
                    "Non-boolean condition in 'while' loop",
                    "Condition expression evaluated to type '" + condType + "', but 'while' requires a boolean condition.",
                    "Use a relational or boolean expression.",
                    node.getPosition().getLine(),
                    node.getPosition().getColumn(),
                    5,
                    "while",
                    "",
                    "SAFE"
            ));
        }
        if (node.getBody() != null) node.getBody().accept(this, context);
        return Type.VOID;
    }

    @Override
    public Type visit(ForNode node, Void context) {
        if (node.getInit() != null) node.getInit().accept(this, context);
        if (node.getCondition() != null) {
            Type condType = node.getCondition().accept(this, context);
            if (condType != Type.BOOL && condType != Type.UNKNOWN) {
                collector.add(new Diagnostic(
                        "SEM005",
                        Category.SEMANTIC,
                        Severity.ERROR,
                        "Non-boolean condition in 'for' loop",
                        "Condition expression evaluated to type '" + condType + "', but 'for' requires a boolean condition.",
                        "Use a relational or boolean expression.",
                        node.getPosition().getLine(),
                        node.getPosition().getColumn(),
                        3,
                        "for",
                        "",
                        "SAFE"
                ));
            }
        }
        if (node.getUpdate() != null) node.getUpdate().accept(this, context);
        if (node.getBody() != null) node.getBody().accept(this, context);
        return Type.VOID;
    }

    @Override
    public Type visit(ReturnNode node, Void context) {
        Type returnExprType = node.getValue() != null ? node.getValue().accept(this, context) : Type.VOID;

        if (currentFunction != null) {
            Type expected = currentFunction.getReturnType();
            if (expected == Type.VOID && returnExprType != Type.VOID) {
                collector.add(new Diagnostic(
                        "SEM010",
                        Category.SEMANTIC,
                        Severity.ERROR,
                        "Unexpected return value in void function '" + currentFunction.getName() + "'",
                        "Function '" + currentFunction.getName() + "' has void return type and should not return a value.",
                        "Use 'return;' without an expression.",
                        node.getPosition().getLine(),
                        node.getPosition().getColumn(),
                        6,
                        "return",
                        "",
                        "SAFE"
                ));
            } else if (expected != Type.VOID && !TypeChecker.isAssignable(expected, returnExprType)) {
                collector.add(new Diagnostic(
                        "SEM010",
                        Category.SEMANTIC,
                        Severity.ERROR,
                        "Invalid return type in function '" + currentFunction.getName() + "'",
                        "Function expected to return '" + expected + "', but returned '" + returnExprType + "'.",
                        "Return an expression of type " + expected + ".",
                        node.getPosition().getLine(),
                        node.getPosition().getColumn(),
                        6,
                        "return",
                        "",
                        "SAFE"
                ));
            }
        }
        return Type.VOID;
    }

    @Override
    public Type visit(ExprStmtNode node, Void context) {
        if (node.getExpression() != null) {
            node.getExpression().accept(this, context);
        }
        return Type.VOID;
    }

    @Override
    public Type visit(BinaryExpr node, Void context) {
        Type leftType = node.getLeft().accept(this, context);
        Type rightType = node.getRight().accept(this, context);
        Type resultType = TypeChecker.getBinaryResultType(node.getOperator(), leftType, rightType);

        if (resultType == Type.UNKNOWN) {
            collector.add(new Diagnostic(
                    "SEM006",
                    Category.SEMANTIC,
                    Severity.ERROR,
                    "Incompatible operands for operator '" + node.getOperator() + "'",
                    "Operator '" + node.getOperator() + "' cannot be applied to operands of types '" + leftType + "' and '" + rightType + "'.",
                    "Ensure compatible types for binary operation.",
                    node.getPosition().getLine(),
                    node.getPosition().getColumn(),
                    node.getOperator().length(),
                    node.getOperator(),
                    "",
                    "SAFE"
            ));
        }

        node.setInferredType(resultType);
        return resultType;
    }

    @Override
    public Type visit(UnaryExpr node, Void context) {
        Type opType = node.getOperand().accept(this, context);
        Type res = TypeChecker.getUnaryResultType(node.getOperator(), opType);
        if (res == Type.UNKNOWN) {
            collector.add(new Diagnostic(
                    "SEM006",
                    Category.SEMANTIC,
                    Severity.ERROR,
                    "Incompatible operand for operator '" + node.getOperator() + "'",
                    "Operator '" + node.getOperator() + "' cannot be applied to operand of type '" + opType + "'.",
                    "Verify operand type.",
                    node.getPosition().getLine(),
                    node.getPosition().getColumn(),
                    node.getOperator().length(),
                    node.getOperator(),
                    "",
                    "SAFE"
            ));
        }
        node.setInferredType(res);
        return res;
    }

    @Override
    public Type visit(LiteralNode node, Void context) {
        return node.getLiteralType();
    }

    @Override
    public Type visit(VarRefNode node, Void context) {
        Symbol symbol = currentScope.resolve(node.getVariableName());
        if (symbol == null) {
            collector.add(new Diagnostic(
                    "SEM001",
                    Category.SEMANTIC,
                    Severity.ERROR,
                    "Undeclared variable: '" + node.getVariableName() + "'",
                    "Variable '" + node.getVariableName() + "' is referenced without prior declaration.",
                    "Declare '" + node.getVariableName() + "' before referencing it.",
                    node.getPosition().getLine(),
                    node.getPosition().getColumn(),
                    node.getVariableName().length(),
                    node.getVariableName(),
                    "",
                    "SAFE"
            ));
            node.setInferredType(Type.UNKNOWN);
            return Type.UNKNOWN;
        }

        if (!symbol.isInitialized()) {
            collector.add(new Diagnostic(
                    "SEM011",
                    Category.SEMANTIC,
                    Severity.WARNING,
                    "Potential use before initialization: '" + node.getVariableName() + "'",
                    "Variable '" + node.getVariableName() + "' is read here, but was declared without an initializer and has not been definitely assigned.",
                    "Initialize '" + node.getVariableName() + "' at declaration.",
                    node.getPosition().getLine(),
                    node.getPosition().getColumn(),
                    node.getVariableName().length(),
                    node.getVariableName(),
                    "",
                    "SAFE"
            ));
        }

        node.setInferredType(symbol.getType());
        return symbol.getType();
    }

    @Override
    public Type visit(CallNode node, Void context) {
        Symbol fnSymbol = symbolTable.getRootScope().resolve(node.getFunctionName());
        if (fnSymbol == null || fnSymbol.getKind() != SymbolKind.FUNCTION) {
            collector.add(new Diagnostic(
                    "SEM007",
                    Category.SEMANTIC,
                    Severity.ERROR,
                    "Undeclared function: '" + node.getFunctionName() + "'",
                    "Function '" + node.getFunctionName() + "' has not been declared.",
                    "Define function '" + node.getFunctionName() + "' before calling it.",
                    node.getPosition().getLine(),
                    node.getPosition().getColumn(),
                    node.getFunctionName().length(),
                    node.getFunctionName(),
                    "",
                    "SAFE"
            ));
            node.setInferredType(Type.UNKNOWN);
            return Type.UNKNOWN;
        }

        List<Type> paramTypes = fnSymbol.getParameterTypes();
        List<ExpressionNode> args = node.getArguments();

        if (args.size() != paramTypes.size()) {
            collector.add(new Diagnostic(
                    "SEM008",
                    Category.SEMANTIC,
                    Severity.ERROR,
                    "Function arity mismatch for '" + node.getFunctionName() + "'",
                    "Function '" + node.getFunctionName() + "' expects " + paramTypes.size() + " arguments, but " + args.size() + " were provided.",
                    "Pass exactly " + paramTypes.size() + " arguments.",
                    node.getPosition().getLine(),
                    node.getPosition().getColumn(),
                    node.getFunctionName().length(),
                    node.getFunctionName(),
                    "",
                    "SAFE"
            ));
        } else {
            for (int i = 0; i < args.size(); i++) {
                Type argType = args.get(i).accept(this, context);
                Type expectedParamType = paramTypes.get(i);
                if (!TypeChecker.isAssignable(expectedParamType, argType)) {
                    collector.add(new Diagnostic(
                            "SEM009",
                            Category.SEMANTIC,
                            Severity.ERROR,
                            "Argument type mismatch in call to '" + node.getFunctionName() + "'",
                            "Argument " + (i + 1) + " evaluates to '" + argType + "', but expected '" + expectedParamType + "'.",
                            "Provide argument of type " + expectedParamType + ".",
                            args.get(i).getPosition().getLine(),
                            args.get(i).getPosition().getColumn(),
                            args.get(i).getPosition().getLength(),
                            args.get(i).toString(),
                            "",
                            "SAFE"
                    ));
                }
            }
        }

        node.setInferredType(fnSymbol.getType());
        return fnSymbol.getType();
    }
}
