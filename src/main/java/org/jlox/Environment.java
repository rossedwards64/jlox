package org.jlox;

import java.util.Map;
import java.util.HashMap;

import static org.jlox.ErrorMessage.UNDEFINED_VARIABLE;

public class Environment {
    private final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();

    public Environment() {
        enclosing = null;
    }

    public Environment(final Environment enclosing) {
        this.enclosing = enclosing;
    }

    public Environment getEnclosing() {
        return enclosing;
    }

    public void define(String name, Object value) {
        values.put(name, value);
    }

    public Environment ancestor(int distance) {
        Environment environment = this;
        for (int i = 0; i < distance; i++) {
            environment = environment.enclosing;
        }
        return environment;
    }

    public Object getAt(int distance, String name) {
        return ancestor(distance).values.get(name);
    }

    public Object get(Token name) {
        if (values.containsKey(name.lexeme())) {
            return values.get(name.lexeme());
        }
        if (enclosing != null) return enclosing.get(name);
        throw environmentError(name, UNDEFINED_VARIABLE);
    }

    public void assignAt(int distance, Token name, Object value) {
        ancestor(distance).values.put(name.lexeme(), value);
    }

    public void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme())) {
            values.put(name.lexeme(), value);
            return;
        }
        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }
        throw environmentError(name, UNDEFINED_VARIABLE);
    }

    private RuntimeError environmentError(Token name, ErrorMessage message) {
        throw new RuntimeError(name,
                String.format(message.getMsg(), name.lexeme()));
    }
}
