package com.codeinsight.compiler.dataflow;

import com.codeinsight.compiler.ast.node.*;
import com.codeinsight.compiler.ast.visitor.BaseASTVisitor;
import com.codeinsight.compiler.cfg.BasicBlock;
import com.codeinsight.compiler.cfg.CFGEdge;
import com.codeinsight.compiler.cfg.ControlFlowGraph;

import java.util.*;

public class LiveVariableAnalysis {

    public static void compute(ControlFlowGraph cfg) {
        // Compute GEN (uses before defs) and KILL (defs)
        for (BasicBlock b : cfg.getBlocks()) {
            b.getGenLive().clear();
            b.getKillLive().clear();

            for (ASTNode node : b.getStatements()) {
                // If statement defines a var:
                if (node instanceof VarDeclNode) {
                    VarDeclNode vd = (VarDeclNode) node;
                    if (vd.getInitializer() != null) {
                        extractUses(vd.getInitializer(), b.getGenLive(), b.getKillLive());
                    }
                    b.getKillLive().add(vd.getName());
                } else if (node instanceof AssignNode) {
                    AssignNode an = (AssignNode) node;
                    if (an.getValue() != null) {
                        extractUses(an.getValue(), b.getGenLive(), b.getKillLive());
                    }
                    b.getKillLive().add(an.getTargetVar());
                } else {
                    extractUses(node, b.getGenLive(), b.getKillLive());
                }
            }
        }

        // Backward iterative worklist
        Queue<BasicBlock> worklist = new LinkedList<>(cfg.getBlocks());
        while (!worklist.isEmpty()) {
            BasicBlock b = worklist.poll();

            // OUT[B] = Union IN[S]
            Set<String> newOut = new HashSet<>();
            for (CFGEdge edge : b.getOutgoingEdges()) {
                newOut.addAll(edge.getTarget().getInLive());
            }
            b.getOutLive().clear();
            b.getOutLive().addAll(newOut);

            // IN[B] = GEN[B] U (OUT[B] \ KILL[B])
            Set<String> newIn = new HashSet<>(b.getGenLive());
            for (String v : b.getOutLive()) {
                if (!b.getKillLive().contains(v)) {
                    newIn.add(v);
                }
            }

            if (!newIn.equals(b.getInLive())) {
                b.getInLive().clear();
                b.getInLive().addAll(newIn);

                for (CFGEdge edge : b.getIncomingEdges()) {
                    BasicBlock pred = edge.getSource();
                    if (!worklist.contains(pred)) {
                        worklist.add(pred);
                    }
                }
            }
        }
    }

    private static void extractUses(ASTNode node, Set<String> gen, Set<String> kill) {
        if (node == null) return;
        node.accept(new BaseASTVisitor<Void, Void>() {
            @Override
            public Void visit(VarRefNode ref, Void ctx) {
                if (!kill.contains(ref.getVariableName())) {
                    gen.add(ref.getVariableName());
                }
                return null;
            }
        }, null);
    }
}
