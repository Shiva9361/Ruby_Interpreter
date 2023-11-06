package ruby;

import java.util.HashMap;
import java.util.Map;

class Environment {
    private final Map<String, Object> values = new HashMap<>();
    final Environment enclosing;

    Environment() {
        enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    Object get(Token name) {
        // for (Map.Entry<String, Object> entry : values.entrySet()) {
        // String key = entry.getKey();
        // String value = entry.getValue().toString();
        // System.out.println("Key: " + key + ", Value: " + value);
        // }
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }
        if (enclosing != null)
            return enclosing.get(name);
        if (name.lexeme.charAt(0) == '$') {
            return null;
        }
        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.");
    }

    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.");
    }

    void define(String name, Object value) {
        if (name.charAt(0) == '$' && enclosing != null) {
            enclosing.define(name, value);
        }
        values.put(name, value);
    }

}
