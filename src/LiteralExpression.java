public class LiteralExpression implements Expression {
    private final Object value;

    public LiteralExpression(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String evaluate(Interpreter interpreter) {
        return value.toString();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
