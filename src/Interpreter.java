import java.util.*;

public class Interpreter {

    private Map<String, Object> variables;

    public Interpreter() {
        variables = new HashMap<>();
    }

    public void declareVariable(String variableName, String dataType, String value) {
        Object typedValue = value;

        switch (dataType) {
            case "NUMERO":
                typedValue = Integer.parseInt(value);
                break;
            case "LETRA":
                typedValue = value.charAt(0);
                break;
            case "TINUOD":
                typedValue = value.equals("OO");
                break;
            case "TIPIK":
                typedValue = Double.parseDouble(value);
                break;
            default:
                throw new RuntimeException("Unsupported data type: " + dataType);
        }

        variables.put(variableName, typedValue);
    }

    public Object getVariableValue(String variableName) {
        if (variables.containsKey(variableName)) {
            return variables.get(variableName);
        } else {
            throw new RuntimeException("Variable " + variableName + " is not declared.");
        }
    }

    public void execute(List<Statement> statements) {
        for (Statement statement : statements) {
            statement.execute(this);
        }
    }

    public void print(String value) {
        System.out.print(value); // Print concatenated result
    }

    public String evaluateExpression(Expression expression) {
        if (expression instanceof VariableExpression) {
            String variableName = ((VariableExpression) expression).getVariableName();
            Object value = getVariableValue(variableName);

            return value.toString();
        } else if (expression instanceof EscapeSequenceExpression) {
            return evaluateEscapeSequence((EscapeSequenceExpression) expression);
        } else if (expression instanceof LiteralExpression) {
            return (String) ((LiteralExpression) expression).getValue();
        } else {
            throw new RuntimeException("Unsupported expression type.");
        }
    }

    private String evaluateEscapeSequence(EscapeSequenceExpression escape) {
        String sequence = escape.getSequence();
        if ("[#]".equals(sequence)) {
            return "#";
        }
        return sequence;
    }

    public String evaluateArithmeticExpression(Expression left, String operator, Expression right) {
        String leftValue = evaluateExpression(left);
        String rightValue = evaluateExpression(right);

        switch (operator) {
            case "+":
                try {
                    int leftInt = Integer.parseInt(leftValue);
                    int rightInt = Integer.parseInt(rightValue);
                    return String.valueOf(leftInt + rightInt);
                } catch (NumberFormatException e) {
                    return leftValue + rightValue;
                }
            default:
                throw new RuntimeException("Unsupported operator: " + operator);
        }
    }

    public boolean evaluateBooleanExpression(String operator, String left, String right) {
        switch (operator) {
            case "OO": // True
                return left.equals(right);
            case "DILI": // False
                return !left.equals(right);
            default:
                throw new RuntimeException("Unsupported boolean operator: " + operator);
        }
    }
}
