package main;

public class Variable {
    private TokenType type;
    private Object value;
    private boolean mutable;

    public Variable(TokenType type, Object value, boolean mutable) {
        this.type = type;
        this.value = value;
        this.mutable = mutable;
    }

    // Getters and setters
    public TokenType getType() {
        return type;
    }

    public void setType(TokenType type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isMutable() {
        return mutable;
    }

    public void setImmutable(boolean immutable) {
        mutable = immutable;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
