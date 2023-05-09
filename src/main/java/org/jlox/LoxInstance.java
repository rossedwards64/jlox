package org.jlox;

import java.util.HashMap;
import java.util.Map;

import static org.jlox.ErrorMessage.UNDEFINED_PROPERTY;

public class LoxInstance {
    private final LoxClass clazz;
    private final Map<String, Object> fields = new HashMap<>();

    LoxInstance(LoxClass clazz) {
        this.clazz = clazz;
    }

    public Object get(Token name) {
        if (fields.containsKey(name.lexeme())) {
            return fields.get(name.lexeme());
        }
        LoxFunction method = clazz.findMethod(name.lexeme());
        if (method != null) return method.bind(this);
        throw new RuntimeError(name,
                String.format(UNDEFINED_PROPERTY.getMsg(),
                        name.lexeme()));
    }

    public void set(Token name, Object value) {
        fields.put(name.lexeme(), value);
    }

    @Override
    public String toString() {
        return clazz.getName() + " instance";
    }
}
