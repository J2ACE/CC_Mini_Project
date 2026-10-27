package com.codeinsight.compiler.dataflow;

import com.codeinsight.compiler.ast.node.*;
import com.codeinsight.compiler.ast.visitor.BaseASTVisitor;
import com.codeinsight.compiler.cfg.BasicBlock;
import com.codeinsight.compiler.cfg.CFGEdge;
import com.codeinsight.compiler.cfg.ControlFlowGraph;

import java.util.*;

public class AvailableExpressions {

    public static void compute(ControlFlowGraph cfg) {
        // Collect all non-trivial binary expressions in CFG
        Set<String> allExpressions = new HashSet<>();
        for (BasicBlock b : cfg.getBlocks()) {
            for (ASTNode node : b.getStatements()) {
                collectExpressions(node, allExpressions);
            }
        }

        // Compute GEN and KILL per block
        for (BasicBlock b : cfg.getBlocks()) {
            b.getGenAvail().clear();
            b.getKillAvail().clear();

            for (ASTNode node : b.getStatements()) {
                String definedVar = null;
                if (node instanceof VarDeclNode) {
                    VarDeclNode vd = (VarDeclNode) node;
                    definedVar = vd.getName();
                    if (vd.getInitializer() != null) {
                        collectExpressions(vd.getInitializer(), b.getGenAvail());
                    }
                } else if (node instanceof AssignNode) {
                    AssignNode an = (AssignNode) node;
                    definedVar = an.getTargetVar();
                    if (an.getValue() != null) {
                        collectExpressions(an.getValue(), b.getGenAvail());
                    }
                } else {
                    collectExpressions(node, b.getGenAvail());
                }

                // If definedVar != null, kill all expressions containing definedVar
                if (definedVar != null) {
                    for (String exprStr : allExpressions) {
                        if (containsVariable(exprStr, definedVar)) {
                            b.getKillAvail().add(exprStr);
                            b.getGenAvail().remove(exprStr);
                        }
                    }
                }
            }
        }

        // Initialize: OUT[ENTRY] = empty, OUT[others] = allExpressions
        for (BasicBlock b : cfg.getBlocks()) {
            b.getInAvail().clear();
            b.getOutAvail().clear();
            if (b.getId().equals("ENTRY")) {
                b.getOutAvail().clear();
            } else {
                b.getOutAvail().addAll(allExpressions);
            }
        }

        // Worklist iteration
        Queue<BasicBlock> worklist = new LinkedList<>(cfg.getBlocks());
        while (!worklist.isEmpty()) {
            BasicBlock b = worklist.poll();
            if (b.getId().equals("ENTRY")) continue;

            // IN[B] = Intersection of OUT[P]
            Set<String> newIn = null;
            for (CFGEdge edge : b.getIncomingEdges()) {
                Set<String> predOut = edge.getSource().getOutAvail();
                if (newIn == null) {
                    newIn = new HashSet<>(predOut);
                } else {
                    newIn.retainAll(predOut);
                }
            }
            if (newIn == null) newIn = new HashSet<>();
            b.getInAvail().clear();
            b.getInAvail().addAll(newIn);

            // OUT[B] = GEN[B] U (IN[B] \ KILL[B])
            Set<String> newOut = new HashSet<>(b.getGenAvail());
            for (String e : b.getInAvail()) {
                if (!b.getKillAvail().contains(e)) {
                    newOut.add(e);
                }
            }

            if (!newOut.equals(b.getOutAvail())) {
                b.getOutAvail().clear();
                b.getOutAvail().addAll(newOut);

                for (CFGEdge edge : b.getOutgoingEdges()) {
                    BasicBlock succ = edge.getTarget();
                    if (!worklist.contains(succ)) {
                        worklist.add(succ);
                    }
                }
            }
        }
    }

    private static void collectExpressions(ASTNode node, Set<String> target) {
        if (node == null) return;
        node.accept(new BaseASTVisitor<Void, Void>() {
            @Override
            public Void visit(BinaryExpr b, Void ctx) {
                target.add(formatExpr(b));
                return super.visit(b, ctx);
            }
        }, null);
    }

    public static String formatExpr(BinaryExpr b) {
        String leftStr = b.getLeft() instanceof BinaryExpr ? "(" + formatExpr((BinaryExpr) b.getLeft()) + ")" : b.getLeft().toString();
        String rightStr = b.getRight() instanceof BinaryExpr ? "(" + formatExpr((BinaryExpr) b.getRight()) + ")" : b.getRight().toString();
        if (b.getLeft() instanceof VarRefNode) leftStr = ((VarRefNode) b.getLeft()).getVariableName();
        if (b.getRight() instanceof VarRefNode) rightStr = ((VarRefNode) b.getRight()).getVariableName();
        if (b.getLeft() instanceof LiteralNode) leftStr = String.valueOf(((LiteralNode) b.getLeft()).getRawValue());
        if (b.getRight() instanceof LiteralNode) rightStr = String.valueOf(((LiteralNode) b.getRight()).getRawValue());
        return leftStr + " " + b.getOperator() + " " + rightStr;
    }

    private static boolean containsVariable(String exprStr, String varName) {
        String[] tokens = exprStr.split("[\\s()+*/%-]+");
        for (String t : tokens) {
            if (t.equals(varName)) return true;
        }
        return false;
    }
}
