package main;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    final Environment enclosing;
    private final Map<String, Variable> values = new HashMap<>();

    Environment() {
        enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    void define(String name, Object value, TokenType type) {
        if (values.containsKey(name)) {
            throw new RuntimeError(new Token(type, name, type, 0),
                    "Variable '" + name + "' is already defined in this scope.");
        }

        Environment current = this.enclosing;
        while (current != null) {
            if (current.values.containsKey(name)) {
                throw new RuntimeError(new Token(type, name, type, 0),
                        "Variable '" + name + "' shadows variable from an outer scope.");
            }
            current = current.enclosing;
        }
        values.put(name, new Variable(type, value));
    }

    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme).getValue();
        }

        if (enclosing != null)
            return enclosing.get(name);

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    @SuppressWarnings("incomplete-switch")
    void assign(Token name, Object value) {
        Environment environment = this;
        while (environment != null) {
            if (environment.values.containsKey(name.lexeme)) {

                Variable existingVar = environment.values.get(name.lexeme);


                TokenType expectedType = existingVar.getType();

                boolean typeMatch = false;
                if (expectedType == TokenType.INT && value instanceof Integer) {
                    typeMatch = true;
                } else if (expectedType == TokenType.FLOAT && value instanceof Double) {
                    typeMatch = true;
                } else if (expectedType == TokenType.CHAR && value instanceof Character) {
                    typeMatch = true;
                } else if (expectedType == TokenType.STRING && value instanceof String) {
                    typeMatch = true;
                } else if (expectedType == TokenType.BOOL && value instanceof Boolean) {
                    typeMatch = true;
                }

                if (!typeMatch) {

                    String valueStr = value == null ? "null" : value.toString();
                    if (value instanceof Boolean) valueStr = valueStr.toUpperCase();
                    if (value instanceof String) valueStr = "\"" + valueStr + "\"";
                    if (value instanceof Character) valueStr = "'" + valueStr + "'";

                    throw new RuntimeError(name,
                            "Type mismatch: Cannot assign value " + valueStr +
                                    " (" + (value == null ? "Null" : value.getClass().getSimpleName()) + ")" +
                                    " to variable '" + name.lexeme + "' of type " + expectedType + ".");
                }

                environment.values.put(name.lexeme, new Variable(expectedType, value));
                return;
            }
            environment = environment.enclosing;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "' during assignment attempt.");
    }
}