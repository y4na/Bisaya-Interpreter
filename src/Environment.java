import java.util.*;

public class Environment {
    private final Map<String, Value> variables = new HashMap<>();

    public void declare(String name, Value value) {
        variables.put(name, value);
    }

    public Value get(String name) {
        return variables.get(name);
    }

    public void set(String name, Value value) {
        variables.put(name, value);
    }
}
