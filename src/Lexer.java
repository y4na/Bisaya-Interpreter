import java.util.*;

import java.util.*;

public class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords = new HashMap<>();
    static {
        keywords.put("SUGOD", TokenType.SUGOD);
        keywords.put("KATAPUSAN", TokenType.KATAPUSAN);
        keywords.put("MUGNA", TokenType.MUGNA);
        keywords.put("NUMERO", TokenType.NUMERO);
        keywords.put("LETRA", TokenType.LETRA);
        keywords.put("TINUOD", TokenType.TINUOD);
        keywords.put("TIPIK", TokenType.TIPIK);
        keywords.put("IPAKITA", TokenType.IPAKITA);
        keywords.put("DAWAT", TokenType.DAWAT);
        keywords.put("KUNG", TokenType.KUNG);
        keywords.put("KUNG_WALA", TokenType.KUNG_WALA);
        keywords.put("KUNG_DILI", TokenType.KUNG_DILI);
        keywords.put("ALANG", TokenType.ALANG_SA);
        keywords.put("PUNDOK", TokenType.PUNDOK);
        keywords.put("UG", TokenType.UG);  // AND
        keywords.put("O", TokenType.O);   // OR
        keywords.put("DILI", TokenType.DILI); // NOT (and BOOLEAN_FALSE depending on context)
        keywords.put("OO", TokenType.BOOLEAN_TRUE); // TRUE
        keywords.put("DILI", TokenType.BOOLEAN_FALSE); // FALSE (conflict with NOT keyword)
    }

    public Lexer(String source) {
        this.source = source;
    }

    public List<Token> tokenize() {
        while (!isAtEnd()) {
            skipWhitespaceAndComments();
            if (isAtEnd()) break;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", line));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void skipWhitespaceAndComments() {
        while (!isAtEnd()) {
            char c = peek();
            if (c == ' ' || c == '\r' || c == '\t') {
                advance();
            } else if (c == '\n') {
                line++;
                advance();
            } else if (c == '-' && peekNext() == '-') {
                skipComment();
            } else {
                break;
            }
        }
    }

    private void skipComment() {
        while (!isAtEnd() && peek() != '\n') {
            advance();
        }
        addToken(TokenType.COMMENT, "-- comment");
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '=': addToken(TokenType.ASSIGN); break;
            case '+': addToken(TokenType.PLUS); break;
            case '-':
                if (peek() == '-') {
                    skipComment();
                } else {
                    addToken(TokenType.MINUS);
                }
                break;
            case ':': addToken(TokenType.COLON); break;
            case '*': addToken(TokenType.STAR); break;
            case ',': addToken(TokenType.COMMA); break;
            case '\'':
                character();
                break;
            case '/': addToken(TokenType.SLASH); break;
            case '%': addToken(TokenType.PERCENT); break;
            case '&': addToken(TokenType.AMPERSAND); break;
            case '$': addToken(TokenType.DOLLAR); break;
            case '(': addToken(TokenType.LPAREN); break;
            case ')': addToken(TokenType.RPAREN); break;
            case '[': addToken(TokenType.LBRACE); break;
            case ']': addToken(TokenType.RBRACE); break;
            case '"': string(); break;
            default:
                if (Character.isDigit(c)) {
                    number(c);
                } else if (isAlpha(c)) {
                    identifier(c);
                } else {
                    System.err.println("Unexpected character: " + c + " at line " + line);
                }
                break;
        }
    }



    private boolean isAlpha(char c) {
        return Character.isLetter(c) || c == '_';
    }

    private void identifier(char firstChar) {
        StringBuilder sb = new StringBuilder();
        sb.append(firstChar);
        while (!isAtEnd() && (isAlphaNumeric(peek()) || peek() == '_')) {
            sb.append(advance());
        }

        String text = sb.toString();
        TokenType type = keywords.getOrDefault(text, TokenType.IDENTIFIER);
        addToken(type, text);
    }

    private char advance() {
        if (isAtEnd()) return '\0';
        return source.charAt(current++);
    }

    private boolean isAlphaNumeric(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private void number(char firstDigit) {
        StringBuilder sb = new StringBuilder();
        sb.append(firstDigit);
        while (!isAtEnd() && Character.isDigit(peek())) {
            sb.append(advance());
        }
        addToken(TokenType.NUMBER, sb.toString());
    }

    private void string() {
        StringBuilder sb = new StringBuilder();
        while (!isAtEnd() && peek() != '"') {
            if (peek() == '\n') line++;
            sb.append(advance());
        }

        if (isAtEnd()) {
            System.err.println("Unterminated string at line " + line);
            return;
        }

        advance();
        addToken(TokenType.STRING, sb.toString());
    }

    private void character() {
        if (isAtEnd()) return;
        char value = advance();
        if (advance() != '\'') {
            System.err.println("Invalid character literal at line " + line);
            return;
        }
        addToken(TokenType.CHAR, String.valueOf(value));
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, String lexeme) {
        if (lexeme == null) {
            lexeme = source.substring(current - 1, current);
        }
        tokens.add(new Token(type, lexeme, line));
    }


}


