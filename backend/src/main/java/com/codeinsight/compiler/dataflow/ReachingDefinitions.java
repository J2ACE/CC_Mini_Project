package com.codeinsight.compiler.dataflow;

import com.codeinsight.compiler.ast.node.*;
import com.codeinsight.compiler.cfg.BasicBlock;
import com.codeinsight.compiler.cfg.CFGEdge;
import com.codeinsight.compiler.cfg.ControlFlowGraph;

import java.util.*;

public class ReachingDefinitions {

    public static void compute(ControlFlowGraph cfg) {
        // Collect all definitions in the CFG: "varName@line"
        Map<String, Set<String>> varToAllDefs = new HashMap<>();

        for (BasicBlock b : cfg.getBlocks()) {
            for (ASTNode node : b.getStatements()) {
                if (node instanceof VarDeclNode) {
                    VarDeclNode vd = (VarDeclNode) node;
                    String def = vd.getName() + "@" + vd.getPosition().getLine();
                    varToAllDefs.computeIfAbsent(vd.getName(), k -> new HashSet<>()).add(def);
                } else if (node instanceof AssignNode) {
                    AssignNode an = (AssignNode) node;
                    String def = an.getTargetVar() + "@" + an.getPosition().getLine();
                    varToAllDefs.computeIfAbsent(an.getTargetVar(), k -> new HashSet<>()).add(def);
                }
            }
        }

        // Compute GEN and KILL per block
        for (BasicBlock b : cfg.getBlocks()) {
            b.getGenReaching().clear();
            b.getKillReaching().clear();

            for (ASTNode node : b.getStatements()) {
                String targetVar = null;
                int line = 0;

                if (node instanceof VarDeclNode) {
                    VarDeclNode vd = (VarDeclNode) node;
                    targetVar = vd.getName();
                    line = vd.getPosition().getLine();
                } else if (node instanceof AssignNode) {
                    AssignNode an = (AssignNode) node;
                    targetVar = an.getTargetVar();
                    line = an.getPosition().getLine();
                }

                if (targetVar != null) {
                    String def = targetVar + "@" + line;
                    // Kill all other definitions of targetVar
                    Set<String> allDefs = varToAllDefs.getOrDefault(targetVar, Collections.emptySet());
                    for (String otherDef : allDefs) {
                        if (!otherDef.equals(def)) {
                            b.getKillReaching().add(otherDef);
                        }
                    }
                    b.getGenReaching().add(def);
                }
            }
        }

        // Iterative worklist
        Queue<BasicBlock> worklist = new LinkedList<>(cfg.getBlocks());
        while (!worklist.isEmpty()) {
            BasicBlock b = worklist.poll();

            // IN[B] = Union OUT[P]
            Set<String> newIn = new HashSet<>();
            for (CFGEdge edge : b.getIncomingEdges()) {
                newIn.addAll(edge.getSource().getOutReaching());
            }
            b.getInReaching().clear();
            b.getInReaching().addAll(newIn);

            // OUT[B] = GEN[B] U (IN[B] \ KILL[B])
            Set<String> newOut = new HashSet<>(b.getGenReaching());
            for (String def : b.getInReaching()) {
                if (!b.getKillReaching().contains(def)) {
                    newOut.add(def);
                }
            }

            if (!newOut.equals(b.getOutReaching())) {
                b.getOutReaching().clear();
                b.getOutReaching().addAll(newOut);

                for (CFGEdge edge : b.getOutgoingEdges()) {
                    BasicBlock succ = edge.getTarget();
                    if (!worklist.contains(succ)) {
                        worklist.add(succ);
                    }
                }
            }
        }
    }
}
