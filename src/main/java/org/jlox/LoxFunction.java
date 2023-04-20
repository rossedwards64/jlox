package org.jlox;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;
    private final Environment closure;

    LoxFunction(final Stmt.Function declaration, final Environment closure) {
        this.declaration = declaration;
        this.closure = closure;
    }

    @Override
    public int arity() {
        return declaration.getParams().size();
    }

    @Override
    public Object call(final Interpreter interpreter, final List<Object> args) {
        Environment environment = new Environment(closure);
        for (int i = 0; i < arity(); i++) {
            environment.define(declaration.getParams().get(i).lexeme(),
                    args.get(i));
        }
        try {
            interpreter.executeBlock(declaration.getBody(), environment);
        } catch (Return returnValue) {
            return returnValue.getValue();
        }
        return null;
    }

    @Override
    public String toString() {
        return "<fn " + declaration.getName().lexeme() + ">";
    }
}
