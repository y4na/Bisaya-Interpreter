import java.util.*;

public class Parser {
    private List<Token> tokens;
    private int currentTokenIndex = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Statement> parse() {
        List<Statement> statements = new ArrayList<>();
        while (currentTokenIndex < tokens.size()) {
            Token token = tokens.get(currentTokenIndex);

            if (token.getType() == TokenType.MUGNA) {
                statements.add(parseDeclaration());
            } else if (token.getType() == TokenType.IPAKITA) {
                statements.add(parseIpakita());
            } else if (token.getType() == TokenType.KATAPUSAN) {
                break;
            } else {
                currentTokenIndex++;
            }
        }
        return statements;
    }

    // Parse variable declaration like "MUGNA NUMERO x = 5"
    private Statement parseDeclaration() {
        currentTokenIndex++; // Skip "MUGNA"
        Token dataType = tokens.get(currentTokenIndex++);
        Token variable = tokens.get(currentTokenIndex++);
        currentTokenIndex++; // Skip "="

        Token value = tokens.get(currentTokenIndex++);
        Expression valueExpression = parseExpression(value);

        return new DeclarationStatement(variable.getValue(), dataType.getValue(), valueExpression);
    }

    private Statement parseIpakita() {
        currentTokenIndex++;
        currentTokenIndex++;

        List<Expression> expressions = new ArrayList<>();
        while (currentTokenIndex < tokens.size()) {
            Token token = tokens.get(currentTokenIndex);

            if (token.getType() == TokenType.EOF || token.getType() == TokenType.NEWLINE) break;

            if (token.getType() == TokenType.IDENTIFIER || token.getType() == TokenType.NUMERO) {
                expressions.add(new VariableExpression(token.getValue()));
            } else if (token.getType() == TokenType.AMPERSAND) {
                currentTokenIndex++;
            } else if (token.getType() == TokenType.SQUARE_BRACKETS) {
                String escapeValue = "[#]";
                expressions.add(new EscapeSequenceExpression(escapeValue));
                currentTokenIndex++;
            }
            currentTokenIndex++;
        }

        return new IpakitaStatement(expressions);
    }

    private Expression parseExpression(Token token) {
        if (token.getType() == TokenType.NUMERO) {
            return new LiteralExpression(Integer.parseInt(token.getValue()));
        } else if (token.getType() == TokenType.LETRA) {
            return new LiteralExpression(token.getValue().charAt(0));
        } else if (token.getType() == TokenType.TINUOD || token.getType() == TokenType.DILI) {
            return new LiteralExpression(Boolean.parseBoolean(token.getValue()));
        } else if (token.getType() == TokenType.IDENTIFIER) {
            return new VariableExpression(token.getValue());
        } else {
            return null;
        }
    }
}
