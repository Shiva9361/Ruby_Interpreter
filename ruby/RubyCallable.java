package ruby;

import java.util.List;

interface RubyCallable {
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments);
}
