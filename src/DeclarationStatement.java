public class DeclarationStatement implements Statement {
    private String variable;
    private String dataType;
    private Expression valueExpression;

    public DeclarationStatement(String variable, String dataType, Expression valueExpression) {
        this.variable = variable;
        this.dataType = dataType;
        this.valueExpression = valueExpression;
    }

    public String getVariable() {
        return variable;
    }

    public String getDataType() {
        return dataType;
    }

    public Expression getValueExpression() {
        return valueExpression;
    }

    @Override
    public String toString() {
        return "MUGNA " + dataType + " " + variable + " = " + valueExpression.toString();
    }

    @Override
    public void execute(Interpreter interpreter) {
        String value = valueExpression.evaluate(interpreter);
        interpreter.declareVariable(variable, dataType, value);
    }
}
