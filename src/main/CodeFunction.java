package main;

import java.util.List;

import main.Stmt.Bool;

public class CodeFunction implements CodeCallable {
    private final Stmt.Function declaration;

    CodeFunction(Stmt.Function declaration) {
        this.declaration = declaration;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(interpreter.globals);

        for (int i = 0; i < declaration.params.size(); i++) {
            Token type = declaration.params.get(i).type;
            Object value = arguments.get(i);

            if (type.type == TokenType.STRING) {
                if (value instanceof String) {
                    environment.define(declaration.params.get(i).name.lexeme, value,
                            declaration.params.get(i).type.type, true);
                } else {
                    throw new RuntimeError(declaration.name,
                            "Incompatible types. Cannot convert " + value + " to String");
                }
            } else if (type.type == TokenType.CHAR) {
                if (value instanceof Character) {
                    environment.define(declaration.params.get(i).name.lexeme, value,
                            declaration.params.get(i).type.type, true);
                } else {
                    throw new RuntimeError(declaration.name,
                            "Incompatible types. Cannot convert " + value + " to Character");
                }
            } else if (type.type == TokenType.INT) {
                if (value instanceof Integer) {
                    environment.define(declaration.params.get(i).name.lexeme, value,
                            declaration.params.get(i).type.type, true);
                } else {
                    throw new RuntimeError(declaration.name,
                            "Incompatible types. Cannot convert " + value + " to Integer");
                }
            } else if (type.type == TokenType.FLOAT) {
                if (value instanceof Double) {
                    environment.define(declaration.params.get(i).name.lexeme, value,
                            declaration.params.get(i).type.type, true);
                } else {
                    throw new RuntimeError(declaration.name,
                            "Incompatible types. Cannot convert " + value + " to Float");
                }
            } else if (type.type == TokenType.BOOL) {
                if (value instanceof Boolean) {
                    environment.define(declaration.params.get(i).name.lexeme, value,
                            declaration.params.get(i).type.type, true);
                } else {
                    throw new RuntimeError(declaration.name,
                            "Incompatible types. Cannot convert " + value + " to Boolean");
                }
            }
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            if (declaration.returnType != null && returnValue != null) {
                if (declaration.returnType.type == TokenType.STRING) {
                    if (returnValue.value instanceof String) {
                        return returnValue.value;
                    }
                    throw new RuntimeError(returnValue.keyword, "Return value must be of type String");
                } else if (declaration.returnType.type == TokenType.CHAR) {
                    if (returnValue.value instanceof Character) {
                        return returnValue.value;
                    }
                    throw new RuntimeError(returnValue.keyword, "Return value must be of type Character");

                } else if (declaration.returnType.type == TokenType.INT) {
                    if (returnValue.value instanceof Integer) {
                        return returnValue.value;
                    }
                    throw new RuntimeError(returnValue.keyword, "Return value must be of type Integer");

                } else if (declaration.returnType.type == TokenType.FLOAT) {
                    if (returnValue.value instanceof Double) {
                        return returnValue.value;
                    }
                    throw new RuntimeError(returnValue.keyword, "Return value must be of type Float");

                } else if (declaration.returnType.type == TokenType.BOOL) {
                    if (returnValue.value instanceof Boolean) {
                        return returnValue.value;
                    }
                    throw new RuntimeError(returnValue.keyword, "Return value must be of type Boolean");
                }
            }

            if (declaration.returnType == null) {
                if (returnValue.value != null) {
                    throw new RuntimeError(returnValue.keyword,
                            "Function with void return type shouldn't return anything. ");
                }

                return null;
            }

        }

        if (declaration.returnType != null) {
            throw new RuntimeError(declaration.returnType, "Function must return a value of type "
                    + declaration.returnType.lexeme + " or remove the return type of the function.");
        }
        return null;
    }

}
