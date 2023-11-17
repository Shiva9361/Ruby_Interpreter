package ruby;

import java.util.List;

interface RubyCallable {
    /*
     * arity will store the number of arguments the function takes
     */
    int arity();
    /*
     *This method is intended to be called when you want to execute the function or method. 
     *The return type is Object, indicating that the result of the function call can be of any type.
    */
    Object call(Interpreter interpreter, List<Object> arguments);
}
