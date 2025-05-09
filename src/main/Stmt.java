package main;

import java.util.List;

abstract class Stmt {
    interface Visitor<R> {
        R visitBlockStmt(Block stmt);

        R visitExpressionStmt(Expression stmt);

        R visitIfStmt(If stmt);

        R visitPrintStmt(Print stmt);

        R visitScanStmt(Scan stmt);

        R visitForStmt(For stmt);

        R visitIntStmt(Int stmt);

        R visitFloatStmt(Float stmt);

        R visitCharStmt(Char stmt);

        R visitBoolStmt(Bool stmt);
    }

    static class Block extends Stmt {
        Block(List<Stmt> statements) {
            this.statements = statements;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }

        final List<Stmt> statements;
    }

    static class Expression extends Stmt {
        Expression(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }

        final Expr expression;
    }

    static class If extends Stmt {
        If(Expr condition, List<Stmt> thenBranch, List<Expr> elseIfConditions, List<List<Stmt>> elseIfBranches,
           List<Stmt> elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseIfConditions = elseIfConditions;
            this.elseIfBranches = elseIfBranches;
            this.elseBranch = elseBranch;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }

        final Expr condition;
        final List<Stmt> thenBranch;
        final List<Expr> elseIfConditions;
        final List<List<Stmt>> elseIfBranches;
        final List<Stmt> elseBranch;
    }

    static class Print extends Stmt {
        Print(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStmt(this);
        }

        final Expr expression;
    }


    static class Scan extends Stmt {
        Scan(List<Token> identifiers) {
            this.identifiers = identifiers;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitScanStmt(this);
        }

        final List<Token> identifiers;
    }

    static class For extends Stmt {
        For(Stmt initializer, Expr condition, Expr increment, List<Stmt> body) {
            this.initializer = initializer;
            this.condition = condition;
            this.increment = increment;
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitForStmt(this);
        }

        final Stmt initializer;
        final Expr condition;
        final Expr increment;
        final List<Stmt> body;
    }


    static class Int extends Stmt {
        Int(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitIntStmt(this);
        }

        final Token name;
        final Expr initializer;
    }

    static class Float extends Stmt {
        Float(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitFloatStmt(this);
        }

        final Token name;
        final Expr initializer;
    }

    static class Char extends Stmt {
        Char(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitCharStmt(this);
        }

        final Token name;
        final Expr initializer;
    }

    static class Bool extends Stmt {
        Bool(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBoolStmt(this);
        }

        final Token name;
        final Expr initializer;
    }

    abstract <R> R accept(Visitor<R> visitor);
}