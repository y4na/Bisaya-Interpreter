package main;

import java.util.List;

abstract class Stmt {
    interface Visitor<R> {
        R visitBlockStmt(Block stmt);

        R visitExpressionStmt(Expression stmt);

        R visitFunctionStmt(Function stmt);

        R visitIfStmt(If stmt);

        R visitPrintStmt(Print stmt);

        R visitReturnStmt(Return stmt);

        R visitScanStmt(Scan stmt);

        R visitWhileStmt(While stmt);

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

    static class Function extends Stmt {
        Function(Token name, List<Parameter> params, List<Stmt> body, Token returnType) {
            this.name = name;
            this.params = params;
            this.body = body;
            this.returnType = returnType;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunctionStmt(this);
        }

        final Token name;
        final List<Parameter> params;
        final List<Stmt> body;
        final Token returnType;

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

    static class Return extends Stmt {
        Return(Token keyword, Expr value) {
            this.keyword = keyword;
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturnStmt(this);
        }

        final Token keyword;
        final Expr value;
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

    static class While extends Stmt {
        While(Expr condition, List<Stmt> body) {
            this.condition = condition;
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStmt(this);
        }

        final Expr condition;
        final List<Stmt> body;
    }

    static class Int extends Stmt {
        Int(Token name, Expr initializer, boolean mutable) {
            this.name = name;
            this.initializer = initializer;
            this.mutable = mutable;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitIntStmt(this);
        }

        final Token name;
        final Expr initializer;
        final boolean mutable;
    }

    static class Float extends Stmt {
        Float(Token name, Expr initializer, boolean mutable) {
            this.name = name;
            this.initializer = initializer;
            this.mutable = mutable;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitFloatStmt(this);
        }

        final Token name;
        final Expr initializer;
        final boolean mutable;
    }

    static class Char extends Stmt {
        Char(Token name, Expr initializer, boolean mutable) {
            this.name = name;
            this.initializer = initializer;
            this.mutable = mutable;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitCharStmt(this);
        }

        final Token name;
        final Expr initializer;
        final boolean mutable;
    }

    static class Bool extends Stmt {
        Bool(Token name, Expr initializer, boolean mutable) {
            this.name = name;
            this.initializer = initializer;
            this.mutable = mutable;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBoolStmt(this);
        }

        final Token name;
        final Expr initializer;
        final boolean mutable;
    }

    abstract <R> R accept(Visitor<R> visitor);
}
