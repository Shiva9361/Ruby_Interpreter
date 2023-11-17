package ruby;

import java.util.HashMap;
import java.util.Map;

class Environment {
    private final Map<String, Object> values = new HashMap<>();
    final Environment enclosing;// for linking scopes together
// two constructors first one is is for global scope , second one for remaining scopes as we  need to link it with prviously existing scope
    Environment() {
        enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }
// this method returns the value of variables present in existing and background scope throws error if varible is not defined or assigned previosly
    Object get(Token name) {
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
// this method assign the value to previously defined variables and thros error when it is not previously assigned
    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme) && !(name.lexeme.charAt(0) >= 'A' && name.lexeme.charAt(0) <= 'Z')) {
            values.put(name.lexeme, value);
            if (enclosing != null) {
                enclosing.assign(name, value);
            }
            return;
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.");
    }
//defines the variable for first assignment and throws error when constant is not defined static scopes or if it is already defined
    void define(String name, Object value) {
        if (name.charAt(0) == '$' && enclosing != null) {
            values.put(name, value);
            enclosing.define(name, value);
        }
        if (values.containsKey(name) && (name.charAt(0) >= 'A' && name.charAt(0) <= 'Z')) {
            throw new RuntimeError("Constant variable can not be changed'" + name + "'.");
        }
        if ((name.charAt(0) >= 'A' && name.charAt(0) <= 'Z') && enclosing != null) {
            throw new RuntimeError("dynamic constant assignment is not allowed");
        }
        // in ruby changing the value in child scope changes it in parent scope as
        // everthing is assignment in ruby
        values.put(name, value);
    }

}
