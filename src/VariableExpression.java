public class VariableExpression implements Expression {
    private String variableName;

    public VariableExpression(String variableName) {
        this.variableName = variableName;
    }

    @Override
    public String evaluate(Interpreter interpreter) {
        return interpreter.getVariableValue(variableName).toString();
    }

    public String getVariableName() {
        return variableName;
    }

    @Override
    public String toString() {
        return variableName;
    }
}
