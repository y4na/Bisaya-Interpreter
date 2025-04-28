package main;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;
    private boolean afterVarDeclaration = false;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();

        consume(TokenType.BEGIN, "Expecting SUGOD.");

        while (match(TokenType.STRING, TokenType.CHAR, TokenType.INT, TokenType.FLOAT, TokenType.BOOL,
                TokenType.DECLARATION)) {
            statements.addAll(varDeclaration());
        }

        while (!isAtEnd() && !check(TokenType.END)) {
            if (check(TokenType.STRING) || check(TokenType.CHAR) || check(TokenType.INT) || check(TokenType.FLOAT)
                    || check(TokenType.BOOL) || check(TokenType.DECLARATION)) {
                afterVarDeclaration = true;
            }
            statements.add(statement());
        }

        consume(TokenType.END, "Expecting KATAPUSAN.");

        if (peek().type != TokenType.EOF) {
            if (peek().type == TokenType.BEGIN) {
                throw error(peek(),
                        "Expecting only one pair of SUGOD and KATAPUSAN blocks");
            } else {
                throw error(peek(),
                        "All statements must be inside SUGOD and KATAPUSAN.");
            }
        }

        return statements;
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = or();

        if (match(TokenType.EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            }

            throw error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr or() {
        Expr expr = and();

        while (match(TokenType.OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and() {
        Expr expr = equality();
        while (match(TokenType.AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Stmt statement() {
        if (match(TokenType.DISPLAY)) {
            consume(TokenType.COLON, "Expecting ':' after IPAKITA");
            return displayStatement();
        }

        if (match(TokenType.SCAN)) {
            consume(TokenType.COLON, "Expecting ':' after DAWAT");
            return scanStatement();
        }

        if (match(TokenType.IF)) {
            return ifStatement();
        }

        if (match(TokenType.WHILE)) {
            return whileStatement();
        }

        return expressionStatement();
    }

    private Stmt displayStatement() {
        Expr value = or();
        return new Stmt.Print(value);
    }

    private Stmt scanStatement() {
        List<Token> identifiers = new ArrayList<>();

        do {
            identifiers.add(consume(TokenType.IDENTIFIER, "Expecting identifier after 'dawat'."));
        } while (match(TokenType.COMMA));

        return new Stmt.Scan(identifiers);
    }

    private Stmt ifStatement() {
        // IF (KUNG)
        consume(TokenType.LEFT_PARENTHESIS, "Expecting '(' after KUNG.");
        Expr condition = or();
        consume(TokenType.RIGHT_PARENTHESIS, "Expecting ')' after condition.");

        consume(TokenType.BLOCK, "Expecting PUNDOK after KUNG condition.");
        consume(TokenType.LEFT_BRACE, "Expecting '{' after PUNDOK.");

        List<Stmt> thenBranch = new ArrayList<>();
        while (!isAtEnd() && !check(TokenType.RIGHT_BRACE)) {
            thenBranch.add(statement());
        }
        consume(TokenType.RIGHT_BRACE, "Expecting '}' after PUNDOK block.");

        // Else if (KUNG DILI)
        List<Expr> elseIfConditions = new ArrayList<>();
        List<List<Stmt>> elseIfBranches = new ArrayList<>();

        boolean foundDili = false;

        while (check(TokenType.IF) && checkNext(TokenType.NOT)) {
            advance();
            advance();

            consume(TokenType.LEFT_PARENTHESIS, "Expecting '(' after KUNG DILI.");
            Expr elseIfCondition = or();
            elseIfConditions.add(elseIfCondition);
            consume(TokenType.RIGHT_PARENTHESIS, "Expecting ')' after KUNG DILI condition.");

            consume(TokenType.BLOCK, "Expecting PUNDOK after KUNG DILI.");
            consume(TokenType.LEFT_BRACE, "Expecting '{' after PUNDOK.");

            List<Stmt> elseIfBranch = new ArrayList<>();
            while (!isAtEnd() && !check(TokenType.RIGHT_BRACE)) {
                elseIfBranch.add(statement());
            }
            elseIfBranches.add(elseIfBranch);
            consume(TokenType.RIGHT_BRACE, "Expecting '}' after PUNDOK block.");

            foundDili = true;
        }

        // Else (KUNG WALA)
        List<Stmt> elseBranch = null;
        if (check(TokenType.IF) && checkNext(TokenType.ELSE)) {

            if (!foundDili) {
                error(peek(), "KUNG WALA should be placed after KUNG DILI.");
            }

            advance();
            advance();

            consume(TokenType.BLOCK, "Expecting PUNDOK after KUNG WALA.");
            consume(TokenType.LEFT_BRACE, "Expecting '{' after PUNDOK.");

            elseBranch = new ArrayList<>();
            while (!isAtEnd() && !check(TokenType.RIGHT_BRACE)) {
                elseBranch.add(statement());
            }
            consume(TokenType.RIGHT_BRACE, "Expecting '}' after PUNDOK block.");
        }

        return new Stmt.If(condition, thenBranch, elseIfConditions, elseIfBranches, elseBranch);
    }

    private boolean checkNext(TokenType type) {
        if (current + 1 >= tokens.size()) return false;
        return tokens.get(current + 1).type == type;
    }

    // this was a helper statement for the PUNDOK but wa na nako gigamit
    private List<Stmt> blockStatements() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd() && !check(TokenType.END)) {
            if (check(TokenType.STRING) || check(TokenType.CHAR) || check(TokenType.INT) || check(TokenType.FLOAT)
                    || check(TokenType.BOOL) || check(TokenType.DECLARATION)) { // Changed IMMUTABLE to DECLARATION
                afterVarDeclaration = true;
            }
            statements.add(statement());
        }
        return statements;
    }

    private Stmt whileStatement() {
        consume(TokenType.LEFT_PARENTHESIS, "Expecting '(' after WHILE.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PARENTHESIS, "Expecting ')' after an expression");

        consume(TokenType.BEGIN, "Expecting a BEGIN after Condition.");
        consume(TokenType.WHILE, "Expecting a WHILE after BEGIN");

        List<Stmt> body = new ArrayList<>();
        while (!isAtEnd() && !check(TokenType.END)) {
            if (check(TokenType.STRING) || check(TokenType.CHAR) || check(TokenType.INT) || check(TokenType.FLOAT)
                    || check(TokenType.BOOL) || check(TokenType.DECLARATION)) {
                afterVarDeclaration = true;
            }
            body.add(statement());
        }

        consume(TokenType.END, "Expecting END after a statement.");
        consume(TokenType.WHILE, "Expecting WHILE after END");

        return new Stmt.While(condition, body);
    }

    private List<Stmt> varDeclaration() {
        Token declaration = previous();
        Token token = previous();
        boolean mutable = true;
        List<Token> names = new ArrayList<>();
        List<Expr> initializers = new ArrayList<>();

        if (declaration.type == TokenType.DECLARATION) { // Check for "MUGNA"
            token = consume(peek().type, "Expecting a variable type after MUGNA (DECLARATION).");
        } else {
           throw error(declaration, "Expecting keyword MUGNA before variable declaration.");
        }

        do {
            Token name = consume(TokenType.IDENTIFIER, "Expect proper variable declaration.");
            names.add(name);
            Expr initializer = null;

            if (match(TokenType.EQUAL)) {
                initializer = expression();
            }

            initializers.add(initializer);
        } while (match(TokenType.COMMA));

        List<Stmt> statements = new ArrayList<>();

        switch (token.type) {
            case CHAR:
                for (int i = 0; i < names.size(); i++) {
                    statements.add(new Stmt.Char(names.get(i), initializers.get(i), mutable));
                }
                break;
            case INT:
                for (int i = 0; i < names.size(); i++) {
                    statements.add(new Stmt.Int(names.get(i), initializers.get(i), mutable));
                }
                break;
            case FLOAT:
                for (int i = 0; i < names.size(); i++) {
                    statements.add(new Stmt.Float(names.get(i), initializers.get(i), mutable));
                }
                break;
            default:
                for (int i = 0; i < names.size(); i++) {
                    statements.add(new Stmt.Bool(names.get(i), initializers.get(i), mutable));
                }
                break;
        }

        return statements;
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        return new Stmt.Expression(expr);
    }

    private Expr equality() {
        Expr expr = comparison();
        while (match(TokenType.NOT_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr comparison() {
        Expr expr = term();
        while (match(TokenType.GREATER_THAN, TokenType.GREATER_THAN_EQUAL, TokenType.LESS_THAN,
                TokenType.LESS_THAN_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(TokenType.MINUS, TokenType.PLUS, TokenType.AMPERSAND)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();
        while (match(TokenType.SLASH, TokenType.STAR, TokenType.MODULO)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(TokenType.NOT, TokenType.MINUS, TokenType.PLUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return call();
    }

    private Expr call() {
        Expr expr = primary();

        if (match(TokenType.LEFT_PARENTHESIS)) {
            expr = finishCall(expr);
        }

        return expr;
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(TokenType.RIGHT_PARENTHESIS)) {
            do {
                if (arguments.size() >= 255) {
                    throw error(peek(), "Can't have more than 255 arguments");
                }
                arguments.add(expression());
            } while (match(TokenType.COMMA));
        }

        Token rightParen = consume(TokenType.RIGHT_PARENTHESIS, "Expecting a parenthesis after a function call.");
        return new Expr.Call(callee, rightParen, arguments);
    }

    private Expr primary() {
        if (match(TokenType.TRUE_LITERAL))
            return new Expr.Literal(true);
        if (match(TokenType.FALSE_LITERAL))
            return new Expr.Literal(false);
        if (match(TokenType.NULL))
            return new Expr.Literal(null);
        if (match(TokenType.STRING_LITERAL, TokenType.CHAR_LITERAL,
                TokenType.INT_LITERAL, TokenType.FLOAT_LITERAL, TokenType.DOLLAR_SIGN))
            return new Expr.Literal(previous().literal);
        if (match(TokenType.LEFT_PARENTHESIS)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PARENTHESIS, "Expect ')' after expression");
            return new Expr.Grouping(expr);
        }
        if (match(TokenType.IDENTIFIER)) {
            return new Expr.Variable(previous());
        }

        String message = "Expect expression.";
        if (afterVarDeclaration) {
            message = "You can only declare variable after the SUGOD block.";
        }
        throw error(peek(), message);
    }

    private Token consume(TokenType type, String message) {
        if (check(type))
            return advance();

        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        Main.error(token, message);
        return new ParseError();
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token peekNext() {
        return tokens.get(current + 1);
    }

    private Token advance() {
        if (!isAtEnd())
            current++;

        return previous();
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd())
            return false;
        return peek().type == type;
    }
}
