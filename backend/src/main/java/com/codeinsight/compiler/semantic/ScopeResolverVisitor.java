package com.codeinsight.compiler.semantic;

import com.codeinsight.compiler.ast.node.*;
import com.codeinsight.compiler.ast.visitor.BaseASTVisitor;
import com.codeinsight.compiler.diagnostic.Category;
import com.codeinsight.compiler.diagnostic.Diagnostic;
import com.codeinsight.compiler.diagnostic.DiagnosticCollector;
import com.codeinsight.compiler.diagnostic.Severity;
import com.codeinsight.compiler.symbol.*;

public class ScopeResolverVisitor extends BaseASTVisitor<Void, Void> {
    private final SymbolTable symbolTable;
    private final DiagnosticCollector collector;

    public ScopeResolverVisitor(SymbolTable symbolTable, DiagnosticCollector collector) {
        this.symbolTable = symbolTable;
        this.collector = collector;
    }

    @Override
    public Void visit(ProgramNode node, Void context) {
        // First pass: register all functions in the global symbol table
        for (FunctionNode fn : node.getFunctions()) {
            Symbol fnSymbol = new Symbol(
                    fn.getName(),
                    fn.getReturnType(),
                    SymbolKind.FUNCTION,
                    fn.getPosition()
            );
            for (ParamNode p : fn.getParameters()) {
                fnSymbol.addParameterType(p.getType());
            }

            if (!symbolTable.define(fnSymbol)) {
                collector.add(new Diagnostic(
                        "SEM003",
                        Category.SEMANTIC,
                        Severity.ERROR,
                        "Duplicate function declaration: '" + fn.getName() + "'",
                        "A function with the identifier '" + fn.getName() + "' is already defined in the global scope.",
                        "Rename one of the conflicting function declarations.",
                        fn.getPosition().getLine(),
                        fn.getPosition().getColumn(),
                        fn.getPosition().getLength(),
                        fn.getName(),
                        "",
                        "SAFE"
                ));
            }
        }

        // Second pass: visit globals and function bodies
        return super.visit(node, context);
    }

    @Override
    public Void visit(FunctionNode node, Void context) {
        symbolTable.enterScope("Function_" + node.getName(), ScopeType.FUNCTION);

        // Define parameters in function scope
        for (ParamNode param : node.getParameters()) {
            Symbol paramSymbol = new Symbol(
                    param.getName(),
                    param.getType(),
                    SymbolKind.PARAMETER,
                    param.getPosition()
            );
            paramSymbol.setInitialized(true); // Parameters are initialized at call time

            if (!symbolTable.define(paramSymbol)) {
                collector.add(new Diagnostic(
                        "SEM003",
                        Category.SEMANTIC,
                        Severity.ERROR,
                        "Duplicate parameter: '" + param.getName() + "'",
                        "Parameter identifier '" + param.getName() + "' is declared more than once in function '" + node.getName() + "'.",
                        "Use unique parameter names.",
                        param.getPosition().getLine(),
                        param.getPosition().getColumn(),
                        param.getPosition().getLength(),
                        param.getName(),
                        "",
                        "SAFE"
                ));
            }
        }

        // Visit body
        if (node.getBody() != null) {
            // Visit statements directly so we don't create an unnecessary extra nested block scope for the root function block
            for (StatementNode stmt : node.getBody().getStatements()) {
                stmt.accept(this, context);
            }
        }

        symbolTable.exitScope();
        return null;
    }

    @Override
    public Void visit(BlockNode node, Void context) {
        symbolTable.enterScope("Block_L" + (symbolTable.getCurrentScope().getLevel() + 1), ScopeType.BLOCK);
        super.visit(node, context);
        symbolTable.exitScope();
        return null;
    }

    @Override
    public Void visit(VarDeclNode node, Void context) {
        String name = node.getName();

        // Check if shadowing variable from outer scope
        Symbol existingOuter = symbolTable.resolve(name);
        if (existingOuter != null && symbolTable.resolveLocally(name) == null) {
            collector.add(new Diagnostic(
                    "SEM002",
                    Category.SEMANTIC,
                    Severity.WARNING,
                    "Variable shadowing: '" + name + "'",
                    "Variable '" + name + "' shadows a previously declared identifier in an enclosing scope at line " + existingOuter.getDeclarationPosition().getLine() + ".",
                    "Consider renaming the local variable to avoid confusion.",
                    node.getPosition().getLine(),
                    node.getPosition().getColumn(),
                    name.length(),
                    name,
                    "",
                    "SAFE"
            ));
        }

        Symbol varSymbol = new Symbol(name, node.getType(), SymbolKind.VARIABLE, node.getPosition());
        if (node.getInitializer() != null) {
            varSymbol.setInitialized(true);
            varSymbol.incrementWriteCount();
            node.getInitializer().accept(this, context);
        }

        if (!symbolTable.define(varSymbol)) {
            collector.add(new Diagnostic(
                    "SEM003",
                    Category.SEMANTIC,
                    Severity.ERROR,
                    "Duplicate declaration: '" + name + "'",
                    "Variable '" + name + "' is already declared in the current scope.",
                    "Remove or rename the duplicate declaration.",
                    node.getPosition().getLine(),
                    node.getPosition().getColumn(),
                    name.length(),
                    name,
                    "",
                    "SAFE"
            ));
        }

        return null;
    }

    @Override
    public Void visit(AssignNode node, Void context) {
        Symbol symbol = symbolTable.resolve(node.getTargetVar());
        if (symbol != null) {
            symbol.setInitialized(true);
            symbol.incrementWriteCount();
        }
        if (node.getValue() != null) {
            node.getValue().accept(this, context);
        }
        return null;
    }

    @Override
    public Void visit(VarRefNode node, Void context) {
        Symbol symbol = symbolTable.resolve(node.getVariableName());
        if (symbol != null) {
            symbol.incrementReadCount();
            symbol.addReference(node.getPosition());
        }
        return null;
    }

    @Override
    public Void visit(CallNode node, Void context) {
        Symbol fnSymbol = symbolTable.resolve(node.getFunctionName());
        if (fnSymbol != null && fnSymbol.getKind() == SymbolKind.FUNCTION) {
            fnSymbol.incrementReadCount();
            fnSymbol.addReference(node.getPosition());
        }
        return super.visit(node, context);
    }
}
