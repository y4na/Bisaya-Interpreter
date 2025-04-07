public class EscapeSequenceExpression implements Expression {
    private String value;

    public EscapeSequenceExpression(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "[" + value + "]";
    }

    @Override
    public String evaluate(Interpreter interpreter) {
        if ("#".equals(value)) {
            return "#";
        }
        return value;
    }

    public String getSequence() {
        return value;
    }
}
