package main;

public class Variable {
    private TokenType type;
    private Object value;
    private boolean mutable;

    public Variable(TokenType type, Object value) {
        this.type = type;
        this.value = value;
    }

    // Getters and setters
    public TokenType getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}