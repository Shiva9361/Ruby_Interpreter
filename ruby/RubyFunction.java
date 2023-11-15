package ruby;

import java.util.List;
  
class RubyFunction implements RubyCallable {

    private final Environment closure;
    private final Stmt.Function declaration;

    RubyFunction(Stmt.Function declaration, Environment closure) {
        this.closure = closure;
        this.declaration = declaration;
    }
    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }
    @Override

    public Object call(Interpreter interpreter,List<Object> arguments) {
        Environment environment = new Environment(closure);
        //Environment environment = new Environment(interpreter.globals);
        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i).lexeme,
            arguments.get(i));
        }
        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            return returnValue.value;
        }
        //interpreter.executeBlock(declaration.body, environment);
        return null;
    }
    

}