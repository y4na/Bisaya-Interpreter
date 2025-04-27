package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("SUGOD", TokenType.BEGIN);
        keywords.put("KATAPUSAN", TokenType.END);
        keywords.put("IPAKITA", TokenType.DISPLAY);
        keywords.put("DAWAT", TokenType.SCAN);
        keywords.put("LETRA", TokenType.CHAR);
        keywords.put("NUMERO", TokenType.INT);
        keywords.put("TIPIK", TokenType.FLOAT);
        keywords.put("TINUOD", TokenType.BOOL);
        keywords.put("KUNG", TokenType.IF);
        keywords.put("KUNG WALA", TokenType.ELSE);
        keywords.put("KUNG DILI", TokenType.ELSE_IF);
        keywords.put("ALANG SA", TokenType.FOR);
        keywords.put("UG", TokenType.AND);
        keywords.put("O", TokenType.OR);
        keywords.put("DILI", TokenType.NOT);
        keywords.put("MUGNA", TokenType.DECLARATION);
        keywords.put("PUNDOK", TokenType.BLOCK);
        keywords.put("OO", TokenType.TRUE_LITERAL);
        keywords.put("DILI", TokenType.FALSE_LITERAL);

        keywords.put("WALA", TokenType.NULL); // not included
    }


    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(':
                addToken(TokenType.LEFT_PARENTHESIS);
                break;
            case ')':
                addToken(TokenType.RIGHT_PARENTHESIS);
                break;
            case '[':
                if (peekNext() == ']') {
                    char escapedCharacter = advance();
                    start += 1;
                    addToken(TokenType.CHAR_LITERAL, escapedCharacter);
                    advance();
                } else {
                    addToken(TokenType.RIGHT_BRACKET);
                }
                break;
            case ']':
                addToken(TokenType.RIGHT_BRACKET);
                break;
            case '{':
                addToken(TokenType.LEFT_BRACE);
                break;
            case '}':
                addToken(TokenType.RIGHT_BRACE);
                break;
            case ',':
                addToken(TokenType.COMMA);
                break;
            case ':':
                addToken(TokenType.COLON);
                break;
            case '+':
                addToken(TokenType.PLUS);
                break;
            case '-':
                addToken(TokenType.MINUS);
                break;
            case '*':
                addToken(TokenType.STAR);
                break;
            case '/':
                addToken(TokenType.SLASH);
                break;
            case '&':
                addToken(TokenType.AMPERSAND);
                break;
            case '$':
                addToken(TokenType.DOLLAR_SIGN, '\n');
                break;
            case '%':
                addToken(TokenType.MODULO);
                break;
            case '=':
                if (match('=')) {
                    addToken(TokenType.EQUAL_EQUAL);
                } else {
                    addToken(TokenType.EQUAL);
                }
                break;
            case '>':
                if (match('=')) {
                    addToken(TokenType.GREATER_THAN_EQUAL);
                } else {
                    addToken(TokenType.GREATER_THAN);
                }
                break;
            case '<':
                if (match('=')) {
                    addToken(TokenType.LESS_THAN_EQUAL);
                } else if (match('>')) {
                    addToken(TokenType.NOT_EQUAL);
                } else {
                    addToken(TokenType.LESS_THAN);
                }
                break;
            case ';':
                addToken(TokenType.SEMICOLON);
                break;
            case '#':
                while (!isAtNewLine() && !isAtEnd())
                    advance();
                break;
            case '\0':
            case ' ':
            case '\t':
            case '\r':
                break;
            case '\n':
                line++;
                break;
            case '"':
                string();
                break;
            case '\'':
                if (peekNext() == '\'') {
                    char character = advance();
                    start += 1;
                    addToken(TokenType.CHAR_LITERAL, character);
                    advance();
                }
                break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Main.error(line, current, "Unexpected character.");
                }
                break;
        }
    }

    private boolean match(char expected) {
        if (isAtEnd())
            return false;
        if (source.charAt(current) != expected)
            return false;
        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd() || isAtNewLine())
            return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (isAtEnd() || isAtNewLine())
            return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAtNewLine() {
        if (current < source.length()) {
            return source.charAt(current) == '\n';
        }
        return false;
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isFloat(String value) {
        if (value.contains(".")) {
            return true;
        }

        return false;
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private char advance() {
        current++;
        return source.charAt(current - 1);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private void string() {
        while (peek() != '"' && !isAtNewLine()) {
            advance();
        }

        if (isAtNewLine()) {
            Main.error(line, current, "Unterminated string.");
            return;
        }

        advance();

        String value = source.substring(start + 1, current - 1);
        if (value.equals("OO")) {
            addToken(TokenType.TRUE_LITERAL, value);
        } else if (value.equals("DILI")) {
            addToken(TokenType.FALSE_LITERAL, value);
        } else {
            addToken(TokenType.STRING_LITERAL, value);
        }
    }

    private void number() {
        while (isDigit(peek())) {
            advance();
        }

        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) {
                advance();
            }
        }

        if (isAlpha(peek())) {
            while (isAlphaNumeric(peek())) {
                advance();
            }
            Main.error(line, current, "Unexpected character found after a number.");
            return;
        }

        String value = source.substring(start, current);

        if (isFloat(value)) {
            addToken(TokenType.FLOAT_LITERAL, Double.parseDouble(value));
        } else {
            addToken(TokenType.INT_LITERAL, Integer.parseInt(value));
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) {
            advance();
        }

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null)
            type = TokenType.IDENTIFIER;
        addToken(type);
    }
}
