package ruby;

import java.util.List;

/*
 * This RubyFunction class implements the RubyCallable
 */
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
/*
 * It creates a new local environment based on the closure, 
 * binds the function's parameters to the provided arguments, and then executes 
 * the function's body within this new environment using the interpreter's executeBlock method.
 */
    public Object call(Interpreter interpreter,List<Object> arguments) {
        Environment environment = new Environment(closure);
        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i).lexeme,
            arguments.get(i));
        }
        //We wrap the call to executeBlock() in a try-catch block
        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            return returnValue.value;
        }
        //interpreter.executeBlock(declaration.body, environment);
        return null;
    }
    

}