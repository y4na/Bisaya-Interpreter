import java.nio.file.*;
import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        String source = Files.readString(Path.of("sample.bpp"));

        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenize();
        System.out.println("Tokens successfully read:");
        for (Token token : tokens) {
            System.out.println(token);
        }

        Parser parser = new Parser(tokens);
        List<Statement> statements = parser.parse();

        Interpreter interpreter = new Interpreter();
        interpreter.execute(statements);
    }
}
