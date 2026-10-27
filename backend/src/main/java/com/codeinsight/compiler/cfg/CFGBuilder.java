package com.codeinsight.compiler.cfg;

import com.codeinsight.compiler.ast.node.*;

import java.util.*;

public class CFGBuilder {
    private int blockCounter = 0;
    private final ControlFlowGraph cfg;
    private final BasicBlock exitBlock;

    public CFGBuilder(String functionName) {
        BasicBlock entry = new BasicBlock("ENTRY");
        this.exitBlock = new BasicBlock("EXIT");
        this.cfg = new ControlFlowGraph(functionName, entry, exitBlock);
        this.cfg.addBlock(entry);
    }

    private BasicBlock newBlock(String prefix) {
        BasicBlock b = new BasicBlock("B" + (blockCounter++) + "_" + prefix);
        cfg.addBlock(b);
        return b;
    }

    public static ControlFlowGraph build(FunctionNode function) {
        CFGBuilder builder = new CFGBuilder(function.getName());
        BasicBlock current = builder.cfg.getEntryBlock();

        if (function.getBody() != null) {
            current = builder.buildBlock(function.getBody(), current);
        }

        // Connect trailing block to EXIT if not already terminated
        if (current != null) {
            current.addOutgoingEdge(new CFGEdge(current, builder.exitBlock, EdgeType.FALLTHROUGH));
        }

        builder.cfg.addBlock(builder.exitBlock);

        // Mark unreachable blocks
        builder.computeReachability();

        // Generate DOT
        builder.cfg.setDotRepresentation(DOTExporter.exportToDot(builder.cfg));
        return builder.cfg;
    }

    private BasicBlock buildBlock(BlockNode block, BasicBlock current) {
        for (StatementNode stmt : block.getStatements()) {
            if (current == null) {
                // Statements after return/break in same block are unreachable
                BasicBlock deadBlock = newBlock("Dead");
                deadBlock.setUnreachable(true);
                current = deadBlock;
            }
            current = buildStatement(stmt, current);
        }
        return current;
    }

    private BasicBlock buildStatement(StatementNode stmt, BasicBlock current) {
        if (stmt instanceof BlockNode) {
            return buildBlock((BlockNode) stmt, current);
        } else if (stmt instanceof VarDeclNode || stmt instanceof AssignNode || stmt instanceof ExprStmtNode) {
            current.addStatement(stmt);
            return current;
        } else if (stmt instanceof IfNode) {
            return buildIf((IfNode) stmt, current);
        } else if (stmt instanceof WhileNode) {
            return buildWhile((WhileNode) stmt, current);
        } else if (stmt instanceof ForNode) {
            return buildFor((ForNode) stmt, current);
        } else if (stmt instanceof ReturnNode) {
            current.addStatement(stmt);
            current.addOutgoingEdge(new CFGEdge(current, exitBlock, EdgeType.RETURN_EDGE));
            return null; // Signals block termination
        }
        return current;
    }

    private BasicBlock buildIf(IfNode ifNode, BasicBlock current) {
        BasicBlock condBlock = newBlock("IfCond");
        condBlock.addStatement(ifNode.getCondition());
        current.addOutgoingEdge(new CFGEdge(current, condBlock, EdgeType.FALLTHROUGH));

        BasicBlock thenBlock = newBlock("Then");
        condBlock.addOutgoingEdge(new CFGEdge(condBlock, thenBlock, EdgeType.BRANCH_TRUE));
        BasicBlock thenAfter = buildStatement(ifNode.getThenBranch(), thenBlock);

        BasicBlock elseAfter = null;
        BasicBlock joinBlock = newBlock("IfJoin");

        if (ifNode.getElseBranch() != null) {
            BasicBlock elseBlock = newBlock("Else");
            condBlock.addOutgoingEdge(new CFGEdge(condBlock, elseBlock, EdgeType.BRANCH_FALSE));
            elseAfter = buildStatement(ifNode.getElseBranch(), elseBlock);
        } else {
            condBlock.addOutgoingEdge(new CFGEdge(condBlock, joinBlock, EdgeType.BRANCH_FALSE));
        }

        if (thenAfter != null) {
            thenAfter.addOutgoingEdge(new CFGEdge(thenAfter, joinBlock, EdgeType.FALLTHROUGH));
        }
        if (elseAfter != null) {
            elseAfter.addOutgoingEdge(new CFGEdge(elseAfter, joinBlock, EdgeType.FALLTHROUGH));
        }

        // If both branches terminated with return, joinBlock is unreachable
        if (thenAfter == null && (ifNode.getElseBranch() != null && elseAfter == null)) {
            joinBlock.setUnreachable(true);
            return null;
        }

        return joinBlock;
    }

    private BasicBlock buildWhile(WhileNode whileNode, BasicBlock current) {
        BasicBlock condBlock = newBlock("WhileCond");
        condBlock.addStatement(whileNode.getCondition());
        current.addOutgoingEdge(new CFGEdge(current, condBlock, EdgeType.FALLTHROUGH));

        BasicBlock bodyBlock = newBlock("WhileBody");
        condBlock.addOutgoingEdge(new CFGEdge(condBlock, bodyBlock, EdgeType.BRANCH_TRUE));

        BasicBlock bodyAfter = buildStatement(whileNode.getBody(), bodyBlock);
        if (bodyAfter != null) {
            bodyAfter.addOutgoingEdge(new CFGEdge(bodyAfter, condBlock, EdgeType.LOOP_BACK));
        }

        BasicBlock afterBlock = newBlock("WhileAfter");
        condBlock.addOutgoingEdge(new CFGEdge(condBlock, afterBlock, EdgeType.BRANCH_FALSE));
        return afterBlock;
    }

    private BasicBlock buildFor(ForNode forNode, BasicBlock current) {
        if (forNode.getInit() != null) {
            current.addStatement(forNode.getInit());
        }

        BasicBlock condBlock = newBlock("ForCond");
        if (forNode.getCondition() != null) {
            condBlock.addStatement(forNode.getCondition());
        }
        current.addOutgoingEdge(new CFGEdge(current, condBlock, EdgeType.FALLTHROUGH));

        BasicBlock bodyBlock = newBlock("ForBody");
        condBlock.addOutgoingEdge(new CFGEdge(condBlock, bodyBlock, EdgeType.BRANCH_TRUE));

        BasicBlock bodyAfter = buildStatement(forNode.getBody(), bodyBlock);

        BasicBlock stepBlock = newBlock("ForStep");
        if (forNode.getUpdate() != null) {
            stepBlock.addStatement(forNode.getUpdate());
        }
        if (bodyAfter != null) {
            bodyAfter.addOutgoingEdge(new CFGEdge(bodyAfter, stepBlock, EdgeType.FALLTHROUGH));
        }
        stepBlock.addOutgoingEdge(new CFGEdge(stepBlock, condBlock, EdgeType.LOOP_BACK));

        BasicBlock afterBlock = newBlock("ForAfter");
        condBlock.addOutgoingEdge(new CFGEdge(condBlock, afterBlock, EdgeType.BRANCH_FALSE));
        return afterBlock;
    }

    private void computeReachability() {
        Set<BasicBlock> visited = new HashSet<>();
        Queue<BasicBlock> queue = new LinkedList<>();

        queue.add(cfg.getEntryBlock());
        visited.add(cfg.getEntryBlock());

        while (!queue.isEmpty()) {
            BasicBlock curr = queue.poll();
            for (CFGEdge edge : curr.getOutgoingEdges()) {
                BasicBlock next = edge.getTarget();
                if (!visited.contains(next)) {
                    visited.add(next);
                    queue.add(next);
                }
            }
        }

        for (BasicBlock b : cfg.getBlocks()) {
            if (!visited.contains(b)) {
                b.setUnreachable(true);
            }
        }
    }
}
