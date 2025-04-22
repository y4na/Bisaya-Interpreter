package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    private static final Interpreter interpreter = new Interpreter();
    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError)
            System.exit(65);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            String linePrompts = "SUGOD " + line + " KATAPUSAN";
            if (line == null)
                break;
            run(linePrompts);
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);

        // List<Token> tokens = scanner.scanTokens();
        // for (Token token : tokens) {
        // System.out.println(token);
        // }

        try {
            List<Token> tokens = scanner.scanTokens();
            Parser parser = new Parser(tokens);
            List<Stmt> statements = parser.parse();
            if (hadError)
                System.exit(65);
            if (hadRuntimeError)
                System.exit(70);
            interpreter.interpret(statements);
        } catch (Exception e) {
        }
    }

    static void error(int line, int col, String message) {
        report(line, col, "", message);
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    private static void report(int line, String where, String message) {
        System.err.println("[Line " + line + "] Syntax Error : " + message);

        hadError = true;
    }

    private static void report(int line, int col, String where, String message) {
        System.err.println("[Line " + line + "] Syntax Error : " + message);
        hadError = true;
    }

    static void runtimeError(RuntimeError e) {
        System.err.println("[Line " + e.token.line + "] Runtime Error : " + e.getMessage());
        hadRuntimeError = true;
    }

}
