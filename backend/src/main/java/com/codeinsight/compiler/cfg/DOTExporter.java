package com.codeinsight.compiler.cfg;

import com.codeinsight.compiler.ast.node.*;

public class DOTExporter {

    public static String exportToDot(ControlFlowGraph cfg) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph CFG_").append(cfg.getFunctionName()).append(" {\n");
        sb.append("  rankdir=TB;\n");
        sb.append("  node [shape=box, fontname=\"Courier\", style=\"filled\", fillcolor=\"#1e1e2e\", fontcolor=\"#cdd6f4\", color=\"#45475a\"];\n");
        sb.append("  edge [fontname=\"Courier\", fontsize=10];\n\n");

        for (BasicBlock block : cfg.getBlocks()) {
            String label = getBlockLabel(block);
            String fillColor = block.isUnreachable() ? "#585b70" : (block.getId().equals("ENTRY") ? "#89b4fa" : (block.getId().equals("EXIT") ? "#f38ba8" : "#1e1e2e"));
            String fontColor = (block.getId().equals("ENTRY") || block.getId().equals("EXIT")) ? "#11111b" : (block.isUnreachable() ? "#a6adc8" : "#cdd6f4");
            String shape = (block.getId().equals("ENTRY") || block.getId().equals("EXIT")) ? "oval" : "box";

            sb.append("  \"").append(block.getId()).append("\" [")
              .append("label=\"").append(escape(label)).append("\", ")
              .append("shape=").append(shape).append(", ")
              .append("fillcolor=\"").append(fillColor).append("\", ")
              .append("fontcolor=\"").append(fontColor).append("\"")
              .append("];\n");
        }

        sb.append("\n");

        for (BasicBlock block : cfg.getBlocks()) {
            for (CFGEdge edge : block.getOutgoingEdges()) {
                String color;
                String label = "";
                switch (edge.getType()) {
                    case BRANCH_TRUE:
                        color = "#a6e3a1"; // Green
                        label = "true";
                        break;
                    case BRANCH_FALSE:
                        color = "#f38ba8"; // Red
                        label = "false";
                        break;
                    case LOOP_BACK:
                        color = "#cba6f7"; // Purple
                        label = "loop";
                        break;
                    case RETURN_EDGE:
                        color = "#fab387"; // Orange
                        label = "return";
                        break;
                    case FALLTHROUGH:
                    default:
                        color = "#89dceb"; // Blue
                        break;
                }

                sb.append("  \"").append(edge.getSource().getId()).append("\" -> \"")
                  .append(edge.getTarget().getId()).append("\" [")
                  .append("color=\"").append(color).append("\", ")
                  .append("fontcolor=\"").append(color).append("\"");

                if (!label.isEmpty()) {
                    sb.append(", label=\"").append(label).append("\"");
                }
                sb.append("];\n");
            }
        }

        sb.append("}\n");
        return sb.toString();
    }

    private static String getBlockLabel(BasicBlock block) {
        StringBuilder sb = new StringBuilder();
        sb.append(block.getId());
        if (block.isUnreachable()) {
            sb.append(" [UNREACHABLE]");
        }
        sb.append("\\n");

        for (ASTNode node : block.getStatements()) {
            sb.append(formatNode(node)).append("\\n");
        }
        return sb.toString();
    }

    private static String formatNode(ASTNode node) {
        if (node instanceof VarDeclNode) {
            VarDeclNode v = (VarDeclNode) node;
            return v.getType() + " " + v.getName() + (v.getInitializer() != null ? " = ..." : ";");
        } else if (node instanceof AssignNode) {
            AssignNode a = (AssignNode) node;
            return a.getTargetVar() + " = ...;";
        } else if (node instanceof ReturnNode) {
            ReturnNode r = (ReturnNode) node;
            return "return" + (r.getValue() != null ? " ...;" : ";");
        } else if (node instanceof ExprStmtNode) {
            return "expr;";
        } else if (node instanceof IfNode) {
            return "if (cond)";
        } else if (node instanceof WhileNode) {
            return "while (cond)";
        } else if (node instanceof ForNode) {
            return "for (..)";
        }
        return "stmt";
    }

    private static String escape(String s) {
        return s.replace("\"", "\\\"");
    }
}
