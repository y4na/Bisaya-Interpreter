package main;
import java.util.List;

public interface CodeCallable {
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments);
}
