public class Value {
    public final String type;
    public Object value;

    public Value(String type, Object value) {
        this.type = type;
        this.value = value;
    }

    public String toString() {
        return value.toString();
    }
}
