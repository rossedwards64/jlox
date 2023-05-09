package org.jlox;

import java.util.List;

import static org.jlox.LoxConstants.SELF;

public class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;
    private final Environment closure;
    private final boolean isInitialiser;

    LoxFunction(final Stmt.Function declaration, final Environment closure,
                final boolean isInitialiser) {
        this.declaration = declaration;
        this.closure = closure;
        this.isInitialiser = isInitialiser;
    }

    public LoxFunction bind(LoxInstance instance) {
        Environment environment = new Environment(closure);
        environment.define(SELF.getName(), instance);
        return new LoxFunction(declaration, environment, isInitialiser);
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
            if (isInitialiser) return closure.getAt(0, SELF.getName());
            return returnValue.getValue();
        }
        if (isInitialiser) return closure.getAt(0, SELF.getName());
        return null;
    }

    @Override
    public String toString() {
        return "<fn " + declaration.getName().lexeme() + ">";
    }
}
