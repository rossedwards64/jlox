package org.jlox;

public class Return extends RuntimeException {
    final private Object value;

    Return(Object value) {
        super(null, null, false, false);
        this.value = value;
    }

    public Object getValue() {
        return value;
    }
}
