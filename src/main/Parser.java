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

            throw error(equals, "Invalid assignment target. Cannot assign to " + expr.getClass().getSimpleName() + ".");
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
        if (match(TokenType.DECLARATION)) {
            return varDeclaration().get(0);
        }
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

        if (match(TokenType.FOR)) {
            consume(TokenType.THE, "Expecting 'SA' after 'ALANG'.");
            return forStatement();
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
        //IF (KUNG)
        consume(TokenType.LEFT_PARENTHESIS, "Expecting '(' after KUNG.");
        Expr condition = expression(); // Parse the main condition.
        consume(TokenType.RIGHT_PARENTHESIS, "Expecting ')' after KUNG condition.");

        consume(TokenType.BLOCK, "Expecting PUNDOK after KUNG condition.");
        consume(TokenType.LEFT_BRACE, "Expecting '{' after PUNDOK.");

        List<Stmt> thenBranch = block();

        // ELSE IF (KUNG DILI)
        List<Expr> elseIfConditions = new ArrayList<>();
        List<List<Stmt>> elseIfBranches = new ArrayList<>();

        while (check(TokenType.IF) && checkNext(TokenType.NOT)) {
            advance();
            advance();

            consume(TokenType.LEFT_PARENTHESIS, "Expecting '(' after KUNG DILI.");
            Expr elseIfCondition = expression(); // Parse the else if condition.
            elseIfConditions.add(elseIfCondition);
            consume(TokenType.RIGHT_PARENTHESIS, "Expecting ')' after KUNG DILI condition.");

            consume(TokenType.BLOCK, "Expecting PUNDOK after KUNG DILI condition.");
            consume(TokenType.LEFT_BRACE, "Expecting '{' after PUNDOK.");

            List<Stmt> elseIfBranch = block();
            elseIfBranches.add(elseIfBranch);
        }

        // ELSE (KUNG WALA)
        List<Stmt> elseBranch = null;

        if (check(TokenType.IF) && checkNext(TokenType.ELSE)) {
            advance();
            advance();

            consume(TokenType.BLOCK, "Expecting PUNDOK after KUNG WALA.");
            consume(TokenType.LEFT_BRACE, "Expecting '{' after PUNDOK.");

            elseBranch = block();
        }

        return new Stmt.If(condition, thenBranch, elseIfConditions, elseIfBranches, elseBranch);
    }

    private Stmt forStatement() {
        consume(TokenType.LEFT_PARENTHESIS, "Expecting '(' after ALANG SA.");

        Stmt initializer;
        if (match(TokenType.DECLARATION)) {
            initializer = varDeclaration().get(0);
        } else if (match(TokenType.IDENTIFIER)) {
            Token name = previous();
            consume(TokenType.EQUAL, "Expect '=' in initializer.");
            Expr value = expression();
            initializer = new Stmt.Expression(new Expr.Assign(name, value));
        } else {
            throw error(peek(), "Invalid initializer in ALANG SA.");
        }

        consume(TokenType.COMMA, "Expect ',' after initializer.");

        Expr condition = expression();
        consume(TokenType.COMMA, "Expect ',' after condition.");

        Expr increment;
        if (match(TokenType.IDENTIFIER)) {
            Token name = previous();
            if (match(TokenType.PLUS_PLUS)) {
                increment = new Expr.Assign(
                        name,
                        new Expr.Binary(
                                new Expr.Variable(name),
                                new Token(TokenType.PLUS, "+", null, name.line),
                                new Expr.Literal(1)
                        )
                );
            } else if (match(TokenType.MINUS_MINUS)) {
                increment = new Expr.Assign(
                        name,
                        new Expr.Binary(
                                new Expr.Variable(name),
                                new Token(TokenType.MINUS, "-", null, name.line),
                                new Expr.Literal(1)
                        )
                );
            } else {
                throw error(peek(), "Expect '++' or '--' after variable in increment.");
            }
        } else {
            throw error(peek(), "Expect variable name in increment.");
        }

        consume(TokenType.RIGHT_PARENTHESIS, "Expect ')' after ALANG SA clauses.");

        consume(TokenType.BLOCK, "Expecting PUNDOK after ALANG SA.");
        consume(TokenType.LEFT_BRACE, "Expecting '{' after PUNDOK.");

        List<Stmt> body = new ArrayList<>();
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            body.add(statement());
        }
        consume(TokenType.RIGHT_BRACE, "Expecting '}' after PUNDOK block.");

        return new Stmt.For(initializer, condition, increment, body);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(statement());
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private void debugPrintTokens(List<Token> tokens) {
        System.out.println("=== DEBUG: TOKENS ===");
        for (Token token : tokens) {
            System.out.println(token.type + " -> " + token.lexeme);
        }
        System.out.println("=====================");
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
            case BOOL:
                for (int i = 0; i < names.size(); i++) {
                    statements.add(new Stmt.Bool(names.get(i), initializers.get(i), mutable));
                }
                break;
            default:
                throw error(declaration, "Unsupported variable type.");
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

    private boolean checkNext(TokenType type) {
        if (current + 1 >= tokens.size()) return false;
        return tokens.get(current + 1).type == type;
    }
}
