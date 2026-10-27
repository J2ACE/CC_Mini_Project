package com.codeinsight.compiler.optimizer;

import com.codeinsight.compiler.ast.node.*;
import com.codeinsight.web.dto.TransformationDto;

import java.util.*;

public class CodeOptimizer {

    public static class Result {
        private final String optimizedCode;
        private final List<TransformationDto> transformations;

        public Result(String optimizedCode, List<TransformationDto> transformations) {
            this.optimizedCode = optimizedCode;
            this.transformations = transformations;
        }

        public String getOptimizedCode() {
            return optimizedCode;
        }

        public List<TransformationDto> getTransformations() {
            return transformations;
        }
    }

    public static Result optimize(ProgramNode ast, String originalSource) {
        if (ast == null) {
            return new Result(originalSource != null ? originalSource : "", Collections.emptyList());
        }

        List<TransformationDto> transformations = new ArrayList<>();
        Map<String, Object> constants = new LinkedHashMap<>();

        // Find variables that are reassigned so we don't inappropriately replace them
        Set<String> reassignedVars = new HashSet<>();
        findReassignedVars(ast, reassignedVars);

        ProgramNode optimizedAst = (ProgramNode) transformNode(ast, constants, reassignedVars, transformations);

        StringBuilder sb = new StringBuilder();
        boolean isTopLevelScript = !originalSource.contains("main(");

        for (VarDeclNode g : optimizedAst.getGlobals()) {
            sb.append(printStatement(g, 0)).append("\n");
        }
        for (FunctionNode f : optimizedAst.getFunctions()) {
            if ("main".equals(f.getName()) && isTopLevelScript) {
                for (StatementNode stmt : f.getBody().getStatements()) {
                    sb.append(printStatement(stmt, 0)).append("\n");
                }
            } else {
                sb.append(printFunction(f)).append("\n");
            }
        }

        return new Result(sb.toString().trim(), transformations);
    }

    private static void findReassignedVars(ASTNode node, Set<String> reassigned) {
        if (node == null) return;
        if (node instanceof AssignNode) {
            reassigned.add(((AssignNode) node).getTargetVar());
        } else if (node instanceof ProgramNode) {
            for (FunctionNode f : ((ProgramNode) node).getFunctions()) findReassignedVars(f, reassigned);
        } else if (node instanceof FunctionNode) {
            findReassignedVars(((FunctionNode) node).getBody(), reassigned);
        } else if (node instanceof BlockNode) {
            for (StatementNode s : ((BlockNode) node).getStatements()) findReassignedVars(s, reassigned);
        } else if (node instanceof IfNode) {
            findReassignedVars(((IfNode) node).getThenBranch(), reassigned);
            if (((IfNode) node).getElseBranch() != null) findReassignedVars(((IfNode) node).getElseBranch(), reassigned);
        } else if (node instanceof WhileNode) {
            findReassignedVars(((WhileNode) node).getBody(), reassigned);
        } else if (node instanceof ForNode) {
            findReassignedVars(((ForNode) node).getBody(), reassigned);
        }
    }

    private static ASTNode transformNode(ASTNode node, Map<String, Object> constants, Set<String> reassignedVars, List<TransformationDto> transformations) {
        if (node instanceof ProgramNode) {
            ProgramNode p = (ProgramNode) node;
            List<VarDeclNode> newGlobals = new ArrayList<>();
            for (VarDeclNode g : p.getGlobals()) {
                newGlobals.add((VarDeclNode) transformNode(g, constants, reassignedVars, transformations));
            }
            List<FunctionNode> newFns = new ArrayList<>();
            for (FunctionNode f : p.getFunctions()) {
                newFns.add((FunctionNode) transformNode(f, constants, reassignedVars, transformations));
            }
            return new ProgramNode(p.getPosition(), newGlobals, newFns);
        }

        if (node instanceof FunctionNode) {
            FunctionNode f = (FunctionNode) node;
            Map<String, Object> localConstants = new LinkedHashMap<>(constants);
            BlockNode newBody = (BlockNode) transformNode(f.getBody(), localConstants, reassignedVars, transformations);
            return new FunctionNode(f.getPosition(), f.getReturnType(), f.getName(), f.getParameters(), newBody);
        }

        if (node instanceof BlockNode) {
            BlockNode b = (BlockNode) node;
            List<StatementNode> newStmts = new ArrayList<>();
            for (StatementNode s : b.getStatements()) {
                newStmts.add((StatementNode) transformNode(s, constants, reassignedVars, transformations));
            }
            return new BlockNode(b.getPosition(), newStmts);
        }

        if (node instanceof VarDeclNode) {
            VarDeclNode v = (VarDeclNode) node;
            ExpressionNode newInit = null;
            if (v.getInitializer() != null) {
                newInit = (ExpressionNode) transformExpr(v.getInitializer(), constants, reassignedVars, transformations, v.getPosition().getLine());
                Object constVal = evalLiteral(newInit);
                if (constVal != null && !reassignedVars.contains(v.getName())) {
                    constants.put(v.getName(), constVal);
                } else {
                    constants.remove(v.getName());
                }
            }
            return new VarDeclNode(v.getPosition(), v.getType(), v.getName(), newInit);
        }

        if (node instanceof AssignNode) {
            AssignNode a = (AssignNode) node;
            ExpressionNode newVal = (ExpressionNode) transformExpr(a.getValue(), constants, reassignedVars, transformations, a.getPosition().getLine());
            constants.remove(a.getTargetVar());
            return new AssignNode(a.getPosition(), a.getTargetVar(), newVal);
        }

        if (node instanceof IfNode) {
            IfNode ifNode = (IfNode) node;
            ExpressionNode newCond = (ExpressionNode) transformExpr(ifNode.getCondition(), constants, reassignedVars, transformations, ifNode.getPosition().getLine());
            // Do not propagate new constants declared inside branches
            StatementNode thenBranch = (StatementNode) transformNode(ifNode.getThenBranch(), new LinkedHashMap<>(), reassignedVars, transformations);
            StatementNode elseBranch = ifNode.getElseBranch() != null ?
                    (StatementNode) transformNode(ifNode.getElseBranch(), new LinkedHashMap<>(), reassignedVars, transformations) : null;
            return new IfNode(ifNode.getPosition(), newCond, thenBranch, elseBranch);
        }

        if (node instanceof WhileNode) {
            WhileNode w = (WhileNode) node;
            ExpressionNode newCond = (ExpressionNode) transformExpr(w.getCondition(), constants, reassignedVars, transformations, w.getPosition().getLine());
            StatementNode body = (StatementNode) transformNode(w.getBody(), new LinkedHashMap<>(constants), reassignedVars, transformations);
            return new WhileNode(w.getPosition(), newCond, body);
        }

        if (node instanceof ReturnNode) {
            ReturnNode r = (ReturnNode) node;
            ExpressionNode val = r.getValue() != null ?
                    (ExpressionNode) transformExpr(r.getValue(), constants, reassignedVars, transformations, r.getPosition().getLine()) : null;
            return new ReturnNode(r.getPosition(), val);
        }

        if (node instanceof ExprStmtNode) {
            ExprStmtNode e = (ExprStmtNode) node;
            ExpressionNode expr = (ExpressionNode) transformExpr(e.getExpression(), constants, reassignedVars, transformations, e.getPosition().getLine());
            return new ExprStmtNode(e.getPosition(), expr);
        }

        return node;
    }

    private static ExpressionNode transformExpr(ExpressionNode expr, Map<String, Object> constants, Set<String> reassignedVars, List<TransformationDto> transformations, int line) {
        if (expr == null) return null;

        // Algebraic Simplification check first (e.g. x + 0 -> x, x * 1 -> x)
        if (expr instanceof BinaryExpr) {
            BinaryExpr b = (BinaryExpr) expr;
            if (b.getOperator().equals("+")) {
                if (isLiteralZero(b.getRight())) {
                    transformations.add(new TransformationDto(
                            "Algebraic Simplification",
                            line > 0 ? line : b.getPosition().getLine(),
                            "Simplified '" + printExpr(b.getLeft()) + " + 0' -> '" + printExpr(b.getLeft()) + "'"
                    ));
                    return b.getLeft();
                } else if (isLiteralZero(b.getLeft())) {
                    transformations.add(new TransformationDto(
                            "Algebraic Simplification",
                            line > 0 ? line : b.getPosition().getLine(),
                            "Simplified '0 + " + printExpr(b.getRight()) + "' -> '" + printExpr(b.getRight()) + "'"
                    ));
                    return b.getRight();
                }
            } else if (b.getOperator().equals("-")) {
                if (isLiteralZero(b.getRight())) {
                    transformations.add(new TransformationDto(
                            "Algebraic Simplification",
                            line > 0 ? line : b.getPosition().getLine(),
                            "Simplified '" + printExpr(b.getLeft()) + " - 0' -> '" + printExpr(b.getLeft()) + "'"
                    ));
                    return b.getLeft();
                }
            } else if (b.getOperator().equals("*")) {
                if (isLiteralOne(b.getRight())) {
                    transformations.add(new TransformationDto(
                            "Algebraic Simplification",
                            line > 0 ? line : b.getPosition().getLine(),
                            "Simplified '" + printExpr(b.getLeft()) + " * 1' -> '" + printExpr(b.getLeft()) + "'"
                    ));
                    return b.getLeft();
                } else if (isLiteralOne(b.getLeft())) {
                    transformations.add(new TransformationDto(
                            "Algebraic Simplification",
                            line > 0 ? line : b.getPosition().getLine(),
                            "Simplified '1 * " + printExpr(b.getRight()) + "' -> '" + printExpr(b.getRight()) + "'"
                    ));
                    return b.getRight();
                } else if (isLiteralZero(b.getRight()) || isLiteralZero(b.getLeft())) {
                    transformations.add(new TransformationDto(
                            "Algebraic Simplification",
                            line > 0 ? line : b.getPosition().getLine(),
                            "Simplified multiplication by 0 -> '0'"
                    ));
                    return new LiteralNode(b.getPosition(), com.codeinsight.compiler.symbol.Type.INT, 0);
                }
            }

            // Now recursively transform left and right
            ExpressionNode l = transformExpr(b.getLeft(), constants, reassignedVars, transformations, line);
            ExpressionNode r = transformExpr(b.getRight(), constants, transformations(b), transformations, line);

            Object lVal = evalLiteral(l);
            Object rVal = evalLiteral(r);

            // Constant Folding: both operands are numeric literals
            if (lVal instanceof Integer && rVal instanceof Integer) {
                int left = (Integer) lVal;
                int right = (Integer) rVal;
                Integer folded = null;
                switch (b.getOperator()) {
                    case "+": folded = left + right; break;
                    case "-": folded = left - right; break;
                    case "*": folded = left * right; break;
                    case "/": if (right != 0) folded = left / right; break;
                    case "%": if (right != 0) folded = left % right; break;
                }
                if (folded != null) {
                    transformations.add(new TransformationDto(
                            "Constant Folding",
                            line > 0 ? line : b.getPosition().getLine(),
                            "Folded '" + left + " " + b.getOperator() + " " + right + "' into constant '" + folded + "'"
                    ));
                    return new LiteralNode(b.getPosition(), com.codeinsight.compiler.symbol.Type.INT, folded);
                }
            }

            return new BinaryExpr(b.getPosition(), b.getOperator(), l, r);
        }

        // Constant Propagation
        if (expr instanceof VarRefNode) {
            VarRefNode v = (VarRefNode) expr;
            if (constants.containsKey(v.getVariableName()) && !reassignedVars.contains(v.getVariableName())) {
                Object val = constants.get(v.getVariableName());
                transformations.add(new TransformationDto(
                        "Constant Propagation",
                        line > 0 ? line : v.getPosition().getLine(),
                        "Propagated constant '" + v.getVariableName() + "' = " + val
                ));
                if (val instanceof Integer) {
                    return new LiteralNode(v.getPosition(), com.codeinsight.compiler.symbol.Type.INT, val);
                } else if (val instanceof Boolean) {
                    return new LiteralNode(v.getPosition(), com.codeinsight.compiler.symbol.Type.BOOL, val);
                }
            }
            return expr;
        }

        return expr;
    }

    private static Set<String> transformations(BinaryExpr b) {
        return Collections.emptySet();
    }

    private static boolean isLiteralZero(ExpressionNode node) {
        if (node instanceof LiteralNode) {
            Object v = ((LiteralNode) node).getRawValue();
            return v instanceof Integer && ((Integer) v) == 0;
        }
        return false;
    }

    private static boolean isLiteralOne(ExpressionNode node) {
        if (node instanceof LiteralNode) {
            Object v = ((LiteralNode) node).getRawValue();
            return v instanceof Integer && ((Integer) v) == 1;
        }
        return false;
    }

    private static Object evalLiteral(ExpressionNode node) {
        if (node instanceof LiteralNode) {
            return ((LiteralNode) node).getRawValue();
        }
        return null;
    }

    private static String indentStr(int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) sb.append("    ");
        return sb.toString();
    }

    private static String printFunction(FunctionNode f) {
        StringBuilder sb = new StringBuilder();
        sb.append(f.getReturnType().toString().toLowerCase()).append(" ").append(f.getName()).append("(");
        for (int i = 0; i < f.getParameters().size(); i++) {
            if (i > 0) sb.append(", ");
            ParamNode p = f.getParameters().get(i);
            sb.append(p.getType().toString().toLowerCase()).append(" ").append(p.getName());
        }
        sb.append(") {\n");
        for (StatementNode s : f.getBody().getStatements()) {
            sb.append(printStatement(s, 1)).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }

    private static String printStatement(StatementNode stmt, int indentLevel) {
        String ind = indentStr(indentLevel);
        if (stmt instanceof VarDeclNode) {
            VarDeclNode v = (VarDeclNode) stmt;
            String res = ind + v.getType().toString().toLowerCase() + " " + v.getName();
            if (v.getInitializer() != null) {
                res += " = " + printExpr(v.getInitializer());
            }
            return res + ";";
        }
        if (stmt instanceof AssignNode) {
            AssignNode a = (AssignNode) stmt;
            return ind + a.getTargetVar() + " = " + printExpr(a.getValue()) + ";";
        }
        if (stmt instanceof IfNode) {
            IfNode ifNode = (IfNode) stmt;
            StringBuilder sb = new StringBuilder();
            sb.append(ind).append("if (").append(printExpr(ifNode.getCondition())).append(") {\n");
            if (ifNode.getThenBranch() instanceof BlockNode) {
                for (StatementNode s : ((BlockNode) ifNode.getThenBranch()).getStatements()) {
                    sb.append(printStatement(s, indentLevel + 1)).append("\n");
                }
            } else {
                sb.append(printStatement(ifNode.getThenBranch(), indentLevel + 1)).append("\n");
            }
            sb.append(ind).append("}");
            if (ifNode.getElseBranch() != null) {
                sb.append(" else {\n");
                if (ifNode.getElseBranch() instanceof BlockNode) {
                    for (StatementNode s : ((BlockNode) ifNode.getElseBranch()).getStatements()) {
                        sb.append(printStatement(s, indentLevel + 1)).append("\n");
                    }
                } else {
                    sb.append(printStatement(ifNode.getElseBranch(), indentLevel + 1)).append("\n");
                }
                sb.append(ind).append("}");
            }
            return sb.toString();
        }
        if (stmt instanceof WhileNode) {
            WhileNode w = (WhileNode) stmt;
            StringBuilder sb = new StringBuilder();
            sb.append(ind).append("while (").append(printExpr(w.getCondition())).append(") {\n");
            if (w.getBody() instanceof BlockNode) {
                for (StatementNode s : ((BlockNode) w.getBody()).getStatements()) {
                    sb.append(printStatement(s, indentLevel + 1)).append("\n");
                }
            } else {
                sb.append(printStatement(w.getBody(), indentLevel + 1)).append("\n");
            }
            sb.append(ind).append("}");
            return sb.toString();
        }
        if (stmt instanceof ReturnNode) {
            ReturnNode r = (ReturnNode) stmt;
            return ind + "return" + (r.getValue() != null ? " " + printExpr(r.getValue()) : "") + ";";
        }
        if (stmt instanceof ExprStmtNode) {
            return ind + printExpr(((ExprStmtNode) stmt).getExpression()) + ";";
        }
        if (stmt instanceof BlockNode) {
            StringBuilder sb = new StringBuilder();
            sb.append(ind).append("{\n");
            for (StatementNode s : ((BlockNode) stmt).getStatements()) {
                sb.append(printStatement(s, indentLevel + 1)).append("\n");
            }
            sb.append(ind).append("}");
            return sb.toString();
        }
        return ind + ";";
    }

    private static String printExpr(ExpressionNode expr) {
        if (expr == null) return "";
        if (expr instanceof VarRefNode) return ((VarRefNode) expr).getVariableName();
        if (expr instanceof LiteralNode) return String.valueOf(((LiteralNode) expr).getRawValue());
        if (expr instanceof BinaryExpr) {
            BinaryExpr b = (BinaryExpr) expr;
            return printExpr(b.getLeft()) + " " + b.getOperator() + " " + printExpr(b.getRight());
        }
        if (expr instanceof UnaryExpr) {
            UnaryExpr u = (UnaryExpr) expr;
            return u.getOperator() + printExpr(u.getOperand());
        }
        if (expr instanceof CallNode) {
            CallNode c = (CallNode) expr;
            StringBuilder sb = new StringBuilder();
            sb.append(c.getFunctionName()).append("(");
            for (int i = 0; i < c.getArguments().size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(printExpr(c.getArguments().get(i)));
            }
            sb.append(")");
            return sb.toString();
        }
        return "expr";
    }
}
