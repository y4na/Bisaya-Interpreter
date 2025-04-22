package main;

import java.util.List;
import java.util.ArrayList;
import java.lang.String;

import main.Expr.Assign;
import main.Expr.Binary;
import main.Expr.Call;
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
import main.Stmt.Function;
import main.Stmt.If;
import main.Stmt.Int;
import main.Stmt.Print;
import main.Stmt.Return;
import main.Stmt.Scan;
import main.Stmt.While;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Object> {

    final Environment globals = new Environment();
    private Environment environment = globals;
    private boolean hasDisplay = false;

    Interpreter() {
        globals.define("clock", new CodeCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double) System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });

        globals.define("ceil", new CodeCallable() {

            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return Math.ceil((double) arguments.get(0));
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });

        globals.define("floor", new CodeCallable() {

            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return Math.floor((double) arguments.get(0));
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });

        globals.define("sqrt", new CodeCallable() {

            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return Math.sqrt((double) arguments.get(0));
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });

        globals.define("abs", new CodeCallable() {

            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return Math.abs((double) arguments.get(0));
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });

        globals.define("pow", new CodeCallable() {

            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return Math.pow((double) arguments.get(0), (double) arguments.get(1));
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });

        globals.define("scanString", new CodeCallable() {

            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                Scanner scanner = new Scanner(System.in);
                System.out.println(arguments.get(0));
                String input = scanner.nextLine();
                scanner.close();
                return input;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });
    }

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                if (statement instanceof Stmt.Print) {
                    hasDisplay = true;
                }
                execute(statement);
            }
            if (!hasDisplay) {
                System.out.println("No Error.");
            }
        } catch (RuntimeError e) {
            Main.runtimeError(e);
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
                String leftValue = (left != null) ? left.toString() : "";
                String rightValue = (right != null) ? right.toString() : "";

                if (left instanceof Boolean)
                    leftValue = left.toString().toUpperCase();
                if (right instanceof Boolean)
                    rightValue = right.toString().toUpperCase();

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
        stmt.accept(this);
    }

    void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;
            for (Stmt statement : statements) {
                if (statement instanceof Stmt.Print) {
                    hasDisplay = true;
                }
                execute(statement);
            }
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
        throw new RuntimeError(operator, "Operand must be an integer or a float nuimber.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if ((left instanceof Integer && right instanceof Integer)
                || (left instanceof Double && right instanceof Double))
            return;
        throw new RuntimeError(operator, "Operand must be an integer or a float nuimber.");
    }

    @Override
    public Void visitExpressionStmt(Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Print stmt) {
        Object value = evaluate(stmt.expression);
        if (value instanceof Boolean) {
            System.out.print(value.toString().toUpperCase());
        } else {
            System.out.print(value.toString());
        }
        return null;
    }

    @Override
    public Object visitStringStmt(Stmt.String stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
            if (!(value instanceof String)) {
                Object v = value;
                if (value instanceof Boolean) {
                    v = value.toString().toUpperCase();
                }
                throw new RuntimeError(stmt.name, "Value '" + v + "' is not of type String.");
            }
        }
        environment.define(stmt.name.lexeme, value, TokenType.STRING, stmt.mutable);
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
        environment.define(stmt.name.lexeme, value, TokenType.INT, stmt.mutable);
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
        environment.define(stmt.name.lexeme, value, TokenType.FLOAT, stmt.mutable);
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
        environment.define(stmt.name.lexeme, value, TokenType.CHAR, stmt.mutable);
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
        environment.define(stmt.name.lexeme, value, TokenType.BOOL, stmt.mutable);
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

        if (isTruthy(evaluate(stmt.condition))) {
            for (Stmt statement : stmt.thenBranch) {
                if (statement instanceof Stmt.Print) {
                    hasDisplay = true;
                }
                execute(statement);
            }
        } else {
            for (int i = 0; i < stmt.elseIfBranches.size(); i++) {
                if (isTruthy(evaluate(stmt.elseIfConditions.get(i)))) {
                    for (Stmt statement : stmt.elseIfBranches.get(i)) {
                        if (statement instanceof Stmt.Print) {
                            hasDisplay = true;
                        }
                        execute(statement);
                        return null;
                    }
                } else {
                    for (Stmt statement : stmt.elseIfBranches.get(i)) {
                        if (statement instanceof Stmt.Print) {
                            hasDisplay = true;
                        }
                    }
                }
            }

            if (stmt.elseBranch != null) {
                for (Stmt statement : stmt.elseBranch) {
                    if (statement instanceof Stmt.Print) {
                        hasDisplay = true;
                    }
                    execute(statement);
                }
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
    public Object visitWhileStmt(While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            for (Stmt statement : stmt.body) {
                if (statement instanceof Stmt.Print) {
                    hasDisplay = true;
                }
                execute(statement);
            }
        }

        return null;
    }

    @Override
    public Object visitFunctionStmt(Function stmt) {
        CodeFunction function = new CodeFunction(stmt);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Object visitScanStmt(Scan stmt) {
        @SuppressWarnings("resource")
        Scanner scanner = new java.util.Scanner(System.in);
        String line = scanner.nextLine();

        List<String> input = List.of(line.split(",")).stream()
                .map(String::trim) // Trim each element using stream API
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
                    if (input.get(i).equals("TRUE")) {
                        parsedInput.add(true);
                    } else if (input.get(i).equals("FALSE")) {
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

    @Override
    public Object visitCallExpr(Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        if (!(callee instanceof CodeCallable)) {
            throw new RuntimeError(expr.paren, "Can only call functions and classes.");
        }

        CodeCallable function = (CodeCallable) callee;
        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren,
                    "Expected " + function.arity() + " arguments but got " + arguments.size() + ".");
        }

        return function.call(this, arguments);
    }

    @Override
    public Object visitReturnStmt(Return stmt) {
        Object value = null;
        if (stmt.value != null)
            value = evaluate(stmt.value);

        throw new main.Return(value, stmt.keyword);
    }
}
