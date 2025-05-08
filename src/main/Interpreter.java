package main;

import java.util.List;
import java.util.ArrayList;
import java.lang.String;

import main.Expr.Assign;
import main.Expr.Binary;
import main.Expr.Grouping;
import main.Expr.Literal;
import main.Expr.Logical;
import main.Expr.Unary;
import main.Expr.Variable;
import main.Stmt.Block;
import main.Stmt.Bool;
import main.Stmt.Char;
import main.Stmt.Expression;
import main.Stmt.Float;
import main.Stmt.If;
import main.Stmt.Int;
import main.Stmt.Print;
import main.Stmt.Scan;
import main.Stmt.For;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Object> {

    final Environment globals = new Environment();
    private Environment environment = globals;
    private boolean hasDisplay = false;

    public void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
            if (!hasDisplay) {
                System.out.println("No Error.");
            }
            if (hasDisplay) {
                System.out.println("\nNo Error.");
            }
        } catch (RuntimeError error) {
            Main.runtimeError(error);
        } catch (Exception e) {
            System.err.println("An unexpected error occurred:");
            e.printStackTrace();
        }
    }



    @Override
    public Object visitBinaryExpr(Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case GREATER_THAN:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left > (int) right;
                } else {
                    return (double) left > (double) right;
                }
            case GREATER_THAN_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left >= (int) right;
                } else {
                    return (double) left >= (double) right;
                }
            case LESS_THAN:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left < (int) right;
                } else {
                    return (double) left < (double) right;
                }
            case LESS_THAN_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left <= (int) right;
                } else {
                    return (double) left <= (double) right;
                }
            case MINUS:
                checkNumberOperands(expr.operator, left, right);

                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left - (int) right;
                } else {
                    return (double) left - (double) right;
                }
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Integer && right instanceof Integer) {
                    if ((int) right == 0) {
                        throw new RuntimeError(expr.operator, "Cannot divide by zero.");
                    } else {
                        return (int) left / (int) right;
                    }
                } else {
                    if ((double) right == 0) {
                        throw new RuntimeError(expr.operator, "Cannot divide by zero.");
                    } else {
                        return (double) left / (double) right;
                    }
                }
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left * (int) right;
                } else {
                    return (double) left * (double) right;
                }
            case PLUS:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left + (int) right;
                } else if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }
                break;
            case AMPERSAND:
                String leftValue = "";
                String rightValue = "";

                if (left != null) {
                    leftValue = stringify(left);
                }

                if (right != null) {
                    rightValue = stringify(right);
                }

                return leftValue + rightValue;

            case MODULO:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left % (int) right;
                } else {
                    return (double) left % (double) right;
                }
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case NOT_EQUAL:
                return !isEqual(left, right);
            default:
                break;
        }

        return null;
    }

    @Override
    public Object visitGroupingExpr(Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case NOT:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                if (right instanceof Integer) {
                    return -(int) right;
                } else {
                    return -(double) right;
                }
            case PLUS:
                checkNumberOperand(expr.operator, right);
                if (right instanceof Integer) {
                    return +(int) right;
                } else {
                    return +(double) right;
                }
            default:
                break;
        }

        return null;
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void execute(Stmt stmt) {
        if (stmt instanceof Stmt.Print) {
            hasDisplay = true;
        }

        stmt.accept(this);
    }

    void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            System.err.println("[Runtime Error in block] " + error.getMessage());

        } finally {
            this.environment = previous;
        }
    }

    private boolean isTruthy(Object object) {
        if (object == null)
            return false;
        if (object instanceof Boolean)
            return (boolean) object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null)
            return true;
        if (a == null)
            return false;

        return a.equals(b);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double || operand instanceof Integer)
            return;
        throw new RuntimeError(operator, "Operand must be an integer or a float number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if ((left instanceof Integer && right instanceof Integer)
                || (left instanceof Double && right instanceof Double))
            return;
        throw new RuntimeError(operator, "Operand must be an integer or a float number.");
    }

    @Override
    public Void visitExpressionStmt(Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    private String stringify(Object value) {
        if (value == null) return "null";

        if (value instanceof Boolean) {
            return (Boolean) value ? "OO" : "DILI";
        }

        return value.toString();
    }

    @Override
    public Void visitPrintStmt(Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.print(stringify(value));
        return null;
    }

    @Override
    public Object visitIntStmt(Int stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
            if (!(value instanceof Integer)) {
                Object v = value;
                if (value instanceof Boolean) {
                    v = value.toString().toUpperCase();
                }
                throw new RuntimeError(stmt.name, "Value '" + v + "' is not of type Integer.");
            }
        }
        environment.define(stmt.name.lexeme, value, TokenType.INT);
        return null;
    }

    @Override
    public Object visitFloatStmt(Float stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
            if (!(value instanceof Double)) {
                Object v = value;
                if (value instanceof Boolean) {
                    v = value.toString().toUpperCase();
                }
                throw new RuntimeError(stmt.name, "Value '" + v + "' is not of type Float.");
            }
        }
        environment.define(stmt.name.lexeme, value, TokenType.FLOAT);
        return null;
    }

    @Override
    public Object visitCharStmt(Char stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
            if (!(value instanceof Character)) {
                Object v = value;
                if (value instanceof Boolean) {
                    v = value.toString().toUpperCase();
                }
                throw new RuntimeError(stmt.name, "Value '" + v + "' is not of type Character.");
            }
        }
        environment.define(stmt.name.lexeme, value, TokenType.CHAR);
        return null;
    }

    @Override
    public Object visitBoolStmt(Bool stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
            if (!(value instanceof Boolean)) {
                throw new RuntimeError(stmt.name, "Value '" + value + "' is not of type Boolean.");
            }
        }
        environment.define(stmt.name.lexeme, value, TokenType.BOOL);
        return null;
    }

    @Override
    public Object visitVariableExpr(Variable expr) {
        return environment.get(expr.name);
    }

    @Override
    public Object visitAssignExpr(Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Object visitBlockStmt(Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));

        return null;
    }

    @Override
    public Object visitIfStmt(If stmt) {
        Object condition = evaluate(stmt.condition);

        if (!(condition instanceof Boolean)) {
            throw new RuntimeError(null, "Condition must be a Boolean.");
        }

        if (isTruthy(condition)) {
            executeBlock(stmt.thenBranch, new Environment(environment));
        } else {
            boolean executedElseIf = false;
            for (int i = 0; i < stmt.elseIfBranches.size(); i++) {
                Object elseIfCondition = evaluate(stmt.elseIfConditions.get(i));
                if (!(elseIfCondition instanceof Boolean)) {
                    throw new RuntimeError(null, "Condition must be a Boolean.");
                }
                if (isTruthy(elseIfCondition)) {
                    executeBlock(stmt.elseIfBranches.get(i), new Environment(environment));
                    executedElseIf = true;
                    break;
                }
            }

            if (!executedElseIf && stmt.elseBranch != null) {
                executeBlock(stmt.elseBranch, new Environment(environment));
            }
        }
        return null;
    }

    @Override
    public Object visitLogicalExpr(Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left))
                return left;
        } else {
            if (!isTruthy(left))
                return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitForStmt(For stmt) {
        if (stmt.initializer != null) {
            execute(stmt.initializer);
        }

        while (true) {
            Object condition = evaluate(stmt.condition);

            if (!(condition instanceof Boolean)) {
                throw new RuntimeError(null, "Condition must be a Boolean."); // E006
            }

            if (!isTruthy(condition)) break;

            executeBlock(stmt.body, new Environment(environment));

            if (stmt.increment != null) {
                evaluate(stmt.increment);
            }
        }

        return null;
    }


    @Override
    public Object visitScanStmt(Scan stmt) {
        @SuppressWarnings("resource")
        Scanner scanner = new java.util.Scanner(System.in);
        String line = scanner.nextLine();

        List<String> input = List.of(line.split(",")).stream()
                .map(String::trim)
                .collect(Collectors.toList());

        List<Object> parsedInput = new ArrayList<>();

        for (int i = 0; i < input.size(); i++) {
            try {
                int intVal = Integer.parseInt(input.get(i));
                parsedInput.add(intVal);
                continue;
            } catch (NumberFormatException e) {
            }

            try {
                double floatVal = Double.parseDouble(input.get(i));
                parsedInput.add(floatVal);
            } catch (NumberFormatException e) {
                if (input.get(i).length() == 1) {
                    char newChar = input.get(i).charAt(0);
                    parsedInput.add(newChar);

                } else {
                    if (input.get(i).equals("OO")) {
                        parsedInput.add(true);
                    } else if (input.get(i).equals("DILI")) {
                        parsedInput.add(false);
                    } else {
                        parsedInput.add(input.get(i));
                    }
                }
            }
        }

        if (input.size() > stmt.identifiers.size()) {
            String m = "value";
            if (stmt.identifiers.size() > 1) {
                m = "values";
            }
            throw new RuntimeError(stmt.identifiers.get(0), "Expected " + stmt.identifiers.size()
                    + " " + m + ". Received more than " + stmt.identifiers.size() + " " + m + ".");
        }

        int current = 0;
        while (current < stmt.identifiers.size()) {
            Object value = parsedInput.get(current);
            environment.assign(stmt.identifiers.get(current), value);
            current++;
        }
        return null;

    }

}