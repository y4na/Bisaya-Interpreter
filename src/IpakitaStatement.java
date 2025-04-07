import java.util.List;

public class IpakitaStatement implements Statement {
    private List<Expression> expressions;

    public IpakitaStatement(List<Expression> expressions) {
        this.expressions = expressions;
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Expression expr : expressions) {
            sb.append(expr.toString()).append(" ");
        }
        return "IPAKITA: " + sb.toString().trim();
    }

    @Override
    public void execute(Interpreter interpreter) {
        StringBuilder result = new StringBuilder();
        for (Expression expr : expressions) {
            result.append(expr.evaluate(interpreter));
        }
        interpreter.print(result.toString());
    }
}
