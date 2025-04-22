package main;

public class Return extends RuntimeException {
    final Object value;
    final Token keyword;

    Return(Object value, Token keyword) {
        super(null, null, false, false);
        this.value = value;
        this.keyword = keyword;
    }
}
