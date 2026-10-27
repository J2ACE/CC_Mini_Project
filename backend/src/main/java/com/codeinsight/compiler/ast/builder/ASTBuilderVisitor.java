package com.codeinsight.compiler.ast.builder;

import com.codeinsight.compiler.ast.node.*;
import com.codeinsight.compiler.grammar.MiniLangBaseVisitor;
import com.codeinsight.compiler.grammar.MiniLangParser;
import com.codeinsight.compiler.symbol.Type;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

public class ASTBuilderVisitor extends MiniLangBaseVisitor<ASTNode> {

    private SourcePosition pos(ParserRuleContext ctx) {
        if (ctx == null) return new SourcePosition(1, 0, 0, 0);
        Token start = ctx.getStart();
        Token stop = ctx.getStop() != null ? ctx.getStop() : start;
        int line = start != null ? start.getLine() : 1;
        int col = start != null ? start.getCharPositionInLine() : 0;
        int startIdx = start != null ? start.getStartIndex() : 0;
        int stopIdx = stop != null ? stop.getStopIndex() : startIdx;
        return new SourcePosition(line, col, startIdx, stopIdx);
    }

    private SourcePosition pos(Token token) {
        if (token == null) return new SourcePosition(1, 0, 0, 0);
        return new SourcePosition(
                token.getLine(),
                token.getCharPositionInLine(),
                token.getStartIndex(),
                token.getStopIndex()
        );
    }

    @Override
    public ASTNode visitProgram(MiniLangParser.ProgramContext ctx) {
        List<VarDeclNode> globals = new ArrayList<>();
        List<FunctionNode> functions = new ArrayList<>();
        List<StatementNode> topLevelStmts = new ArrayList<>();

        if (ctx.globalDecl() != null) {
            for (MiniLangParser.GlobalDeclContext gCtx : ctx.globalDecl()) {
                ASTNode node = visit(gCtx.varDecl());
                if (node instanceof VarDeclNode) {
                    globals.add((VarDeclNode) node);
                }
            }
        }

        if (ctx.functionDecl() != null) {
            for (MiniLangParser.FunctionDeclContext fCtx : ctx.functionDecl()) {
                ASTNode node = visit(fCtx);
                if (node instanceof FunctionNode) {
                    functions.add((FunctionNode) node);
                }
            }
        }

        if (ctx.statement() != null) {
            for (MiniLangParser.StatementContext sCtx : ctx.statement()) {
                ASTNode node = visit(sCtx);
                if (node instanceof StatementNode) {
                    topLevelStmts.add((StatementNode) node);
                }
            }
        }

        if (!topLevelStmts.isEmpty()) {
            BlockNode mainBody = new BlockNode(pos(ctx), topLevelStmts);
            FunctionNode mainFn = new FunctionNode(pos(ctx), Type.INT, "main", java.util.Collections.emptyList(), mainBody);
            functions.add(mainFn);
        }

        return new ProgramNode(pos(ctx), globals, functions);
    }

    @Override
    public ASTNode visitFunctionDecl(MiniLangParser.FunctionDeclContext ctx) {
        Type returnType = parseType(ctx.typeType());
        String name = ctx.ID().getText();

        List<ParamNode> params = new ArrayList<>();
        if (ctx.paramList() != null && ctx.paramList().param() != null) {
            for (MiniLangParser.ParamContext pCtx : ctx.paramList().param()) {
                Type pType = parseType(pCtx.typeType());
                String pName = pCtx.ID().getText();
                params.add(new ParamNode(pos(pCtx), pType, pName));
            }
        }

        BlockNode body = (BlockNode) visit(ctx.block());
        return new FunctionNode(pos(ctx), returnType, name, params, body);
    }

    @Override
    public ASTNode visitBlock(MiniLangParser.BlockContext ctx) {
        List<StatementNode> statements = new ArrayList<>();
        if (ctx.statement() != null) {
            for (MiniLangParser.StatementContext sCtx : ctx.statement()) {
                ASTNode node = visit(sCtx);
                if (node instanceof StatementNode) {
                    statements.add((StatementNode) node);
                }
            }
        }
        return new BlockNode(pos(ctx), statements);
    }

    @Override
    public ASTNode visitBlockStmt(MiniLangParser.BlockStmtContext ctx) {
        return visit(ctx.block());
    }

    @Override
    public ASTNode visitVarDeclStmt(MiniLangParser.VarDeclStmtContext ctx) {
        return visit(ctx.varDecl());
    }

    @Override
    public ASTNode visitVarDecl(MiniLangParser.VarDeclContext ctx) {
        Type type = parseType(ctx.typeType());
        String name = ctx.ID().getText();
        ExpressionNode init = null;
        if (ctx.expression() != null) {
            init = (ExpressionNode) visit(ctx.expression());
        }
        return new VarDeclNode(pos(ctx), type, name, init);
    }

    @Override
    public ASTNode visitAssignStmt(MiniLangParser.AssignStmtContext ctx) {
        return visit(ctx.assignment());
    }

    @Override
    public ASTNode visitAssignment(MiniLangParser.AssignmentContext ctx) {
        String targetVar = ctx.ID().getText();
        ExpressionNode val = (ExpressionNode) visit(ctx.expression());
        return new AssignNode(pos(ctx), targetVar, val);
    }

    @Override
    public ASTNode visitIfStmt(MiniLangParser.IfStmtContext ctx) {
        ExpressionNode cond = (ExpressionNode) visit(ctx.expression());
        StatementNode thenBranch = (StatementNode) visit(ctx.statement(0));
        StatementNode elseBranch = null;
        if (ctx.statement().size() > 1) {
            elseBranch = (StatementNode) visit(ctx.statement(1));
        }
        return new IfNode(pos(ctx), cond, thenBranch, elseBranch);
    }

    @Override
    public ASTNode visitWhileStmt(MiniLangParser.WhileStmtContext ctx) {
        ExpressionNode cond = (ExpressionNode) visit(ctx.expression());
        StatementNode body = (StatementNode) visit(ctx.statement());
        return new WhileNode(pos(ctx), cond, body);
    }

    @Override
    public ASTNode visitForStmt(MiniLangParser.ForStmtContext ctx) {
        ASTNode init = null;
        if (ctx.forInit() != null) {
            if (ctx.forInit().varDeclNoSemi() != null) {
                MiniLangParser.VarDeclNoSemiContext vCtx = ctx.forInit().varDeclNoSemi();
                Type type = parseType(vCtx.typeType());
                String name = vCtx.ID().getText();
                ExpressionNode val = vCtx.expression() != null ? (ExpressionNode) visit(vCtx.expression()) : null;
                init = new VarDeclNode(pos(vCtx), type, name, val);
            } else if (ctx.forInit().assignment() != null) {
                init = visit(ctx.forInit().assignment());
            }
        }

        ExpressionNode cond = ctx.expression() != null ? (ExpressionNode) visit(ctx.expression()) : null;

        ASTNode update = null;
        if (ctx.forUpdate() != null) {
            if (ctx.forUpdate().assignment() != null) {
                update = visit(ctx.forUpdate().assignment());
            } else if (ctx.forUpdate().expression() != null) {
                update = visit(ctx.forUpdate().expression());
            }
        }

        StatementNode body = (StatementNode) visit(ctx.statement());
        return new ForNode(pos(ctx), init, cond, update, body);
    }

    @Override
    public ASTNode visitReturnStmt(MiniLangParser.ReturnStmtContext ctx) {
        ExpressionNode val = null;
        if (ctx.expression() != null) {
            val = (ExpressionNode) visit(ctx.expression());
        }
        return new ReturnNode(pos(ctx), val);
    }

    @Override
    public ASTNode visitExprStmt(MiniLangParser.ExprStmtContext ctx) {
        ExpressionNode expr = (ExpressionNode) visit(ctx.expression());
        return new ExprStmtNode(pos(ctx), expr);
    }

    @Override
    public ASTNode visitEmptyStmt(MiniLangParser.EmptyStmtContext ctx) {
        return new BlockNode(pos(ctx), new ArrayList<>());
    }

    // Expressions
    @Override
    public ASTNode visitLogicalOrExpr(MiniLangParser.LogicalOrExprContext ctx) {
        ExpressionNode left = (ExpressionNode) visit(ctx.expression(0));
        ExpressionNode right = (ExpressionNode) visit(ctx.expression(1));
        return new BinaryExpr(pos(ctx), "||", left, right);
    }

    @Override
    public ASTNode visitLogicalAndExpr(MiniLangParser.LogicalAndExprContext ctx) {
        ExpressionNode left = (ExpressionNode) visit(ctx.expression(0));
        ExpressionNode right = (ExpressionNode) visit(ctx.expression(1));
        return new BinaryExpr(pos(ctx), "&&", left, right);
    }

    @Override
    public ASTNode visitEqualityExpr(MiniLangParser.EqualityExprContext ctx) {
        String op = ctx.getChild(1).getText();
        ExpressionNode left = (ExpressionNode) visit(ctx.expression(0));
        ExpressionNode right = (ExpressionNode) visit(ctx.expression(1));
        return new BinaryExpr(pos(ctx), op, left, right);
    }

    @Override
    public ASTNode visitRelationalExpr(MiniLangParser.RelationalExprContext ctx) {
        String op = ctx.getChild(1).getText();
        ExpressionNode left = (ExpressionNode) visit(ctx.expression(0));
        ExpressionNode right = (ExpressionNode) visit(ctx.expression(1));
        return new BinaryExpr(pos(ctx), op, left, right);
    }

    @Override
    public ASTNode visitAdditiveExpr(MiniLangParser.AdditiveExprContext ctx) {
        String op = ctx.getChild(1).getText();
        ExpressionNode left = (ExpressionNode) visit(ctx.expression(0));
        ExpressionNode right = (ExpressionNode) visit(ctx.expression(1));
        return new BinaryExpr(pos(ctx), op, left, right);
    }

    @Override
    public ASTNode visitMultiplicativeExpr(MiniLangParser.MultiplicativeExprContext ctx) {
        String op = ctx.getChild(1).getText();
        ExpressionNode left = (ExpressionNode) visit(ctx.expression(0));
        ExpressionNode right = (ExpressionNode) visit(ctx.expression(1));
        return new BinaryExpr(pos(ctx), op, left, right);
    }

    @Override
    public ASTNode visitUnaryExpr(MiniLangParser.UnaryExprContext ctx) {
        String op = ctx.getChild(0).getText();
        ExpressionNode operand = (ExpressionNode) visit(ctx.expression());
        return new UnaryExpr(pos(ctx), op, operand);
    }

    @Override
    public ASTNode visitParenExpr(MiniLangParser.ParenExprContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public ASTNode visitCallExpr(MiniLangParser.CallExprContext ctx) {
        String fnName = ctx.ID().getText();
        List<ExpressionNode> args = new ArrayList<>();
        if (ctx.argList() != null && ctx.argList().expression() != null) {
            for (MiniLangParser.ExpressionContext aCtx : ctx.argList().expression()) {
                args.add((ExpressionNode) visit(aCtx));
            }
        }
        return new CallNode(pos(ctx), fnName, args);
    }

    @Override
    public ASTNode visitVarRefExpr(MiniLangParser.VarRefExprContext ctx) {
        return new VarRefNode(pos(ctx), ctx.ID().getText());
    }

    @Override
    public ASTNode visitLiteralExpr(MiniLangParser.LiteralExprContext ctx) {
        return visit(ctx.literal());
    }

    @Override
    public ASTNode visitIntLit(MiniLangParser.IntLitContext ctx) {
        int val = 0;
        try {
            val = Integer.parseInt(ctx.getText());
        } catch (NumberFormatException ignored) {}
        return new LiteralNode(pos(ctx), Type.INT, val);
    }

    @Override
    public ASTNode visitFloatLit(MiniLangParser.FloatLitContext ctx) {
        float val = 0.0f;
        try {
            val = Float.parseFloat(ctx.getText());
        } catch (NumberFormatException ignored) {}
        return new LiteralNode(pos(ctx), Type.FLOAT, val);
    }

    @Override
    public ASTNode visitCharLit(MiniLangParser.CharLitContext ctx) {
        String txt = ctx.getText();
        char c = ' ';
        if (txt.length() >= 3) {
            c = txt.charAt(1);
        }
        return new LiteralNode(pos(ctx), Type.CHAR, c);
    }

    @Override
    public ASTNode visitBoolLit(MiniLangParser.BoolLitContext ctx) {
        boolean val = Boolean.parseBoolean(ctx.getText());
        return new LiteralNode(pos(ctx), Type.BOOL, val);
    }

    private Type parseType(MiniLangParser.TypeTypeContext ctx) {
        if (ctx == null) return Type.UNKNOWN;
        return Type.fromString(ctx.getText());
    }
}
