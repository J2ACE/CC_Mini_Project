package com.codeinsight.compiler.dataflow;

import com.codeinsight.compiler.cfg.BasicBlock;
import com.codeinsight.compiler.cfg.CFGEdge;
import com.codeinsight.compiler.cfg.ControlFlowGraph;

import java.util.*;

public class FixedPointSolver {

    public static <T> void solve(ControlFlowGraph cfg, DataFlowFramework<T> framework) {
        if (framework.isForward()) {
            solveForward(cfg, framework);
        } else {
            solveBackward(cfg, framework);
        }
    }

    private static <T> void solveForward(ControlFlowGraph cfg, DataFlowFramework<T> framework) {
        Queue<BasicBlock> worklist = new LinkedList<>(cfg.getBlocks());
        int maxIterations = 500;
        int iter = 0;

        while (!worklist.isEmpty() && iter++ < maxIterations) {
            BasicBlock block = worklist.poll();

            List<BasicBlock> preds = new ArrayList<>();
            for (CFGEdge edge : block.getIncomingEdges()) {
                preds.add(edge.getSource());
            }

            Set<T> newIn = preds.isEmpty() ? framework.getInitialIn(block) : framework.confluence(preds);
            Set<T> newOut = framework.transfer(block, newIn);

            if (!newOut.equals(block.getOutReaching())) {
                for (CFGEdge edge : block.getOutgoingEdges()) {
                    BasicBlock succ = edge.getTarget();
                    if (!worklist.contains(succ)) {
                        worklist.add(succ);
                    }
                }
            }
        }
    }

    private static <T> void solveBackward(ControlFlowGraph cfg, DataFlowFramework<T> framework) {
        Queue<BasicBlock> worklist = new LinkedList<>(cfg.getBlocks());
        int maxIterations = 500;
        int iter = 0;

        while (!worklist.isEmpty() && iter++ < maxIterations) {
            BasicBlock block = worklist.poll();

            List<BasicBlock> succs = new ArrayList<>();
            for (CFGEdge edge : block.getOutgoingEdges()) {
                succs.add(edge.getTarget());
            }

            Set<T> newOut = succs.isEmpty() ? framework.getInitialOut(block) : framework.confluence(succs);
            Set<T> newIn = framework.transfer(block, newOut);

            if (!newIn.equals(block.getInLive())) {
                for (CFGEdge edge : block.getIncomingEdges()) {
                    BasicBlock pred = edge.getSource();
                    if (!worklist.contains(pred)) {
                        worklist.add(pred);
                    }
                }
            }
        }
    }
}
