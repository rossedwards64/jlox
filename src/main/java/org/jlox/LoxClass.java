package org.jlox;

import java.util.List;
import java.util.Map;

public class LoxClass implements LoxCallable {
    private final String name;
    private final Map<String, LoxFunction> methods;

    LoxClass(final String name,
             final Map<String, LoxFunction> methods) {
        this.name = name;
        this.methods = methods;
    }

    public LoxFunction findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }
        return null;
    }

    public String getName() {
        return name;
    }

    @Override
    public int arity() {
        LoxFunction initialiser = getInitialiser();
        if (initialiser == null) return 0;
        return initialiser.arity();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        LoxInstance instance = new LoxInstance(this);
        LoxFunction initialiser = getInitialiser();
        if (initialiser != null) {
            initialiser.bind(instance).call(interpreter, args);
        }
        return instance;
    }

    private LoxFunction getInitialiser() {
        return findMethod("init");
    }

    @Override
    public String toString() {
        return name;
    }
}
