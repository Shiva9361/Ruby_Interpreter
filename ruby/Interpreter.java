package ruby;

import static ruby.TokenType.EQUAL;

import java.util.ArrayList;
import java.util.List;
import java.lang.Math;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    //This class extends RunTimeException to throw an exception if we encounter a break statement 
    private static class BreakException extends RuntimeException {
        BreakException(String message) {
            super(message);
        }
    }
    //throw a next exception if we encounter a next statement 
    private static class NextException extends RuntimeException {
        NextException(String message) {
            super(message);
        }
    }
/*
 * The environment field in the interpreter changes as we enter and exit local scopes. 
 * It tracks the current environment. 
 * This new globals field holds a fixed reference to the outermost global environment.
 */
    final Environment globals = new Environment();
    private Environment environment = globals;
    // Constructor
    Interpreter() {
        globals.define("clock", new RubyCallable() {
            @Override
            public int arity() { return 0; }
            @Override
            public Object call(Interpreter interpreter,List<Object> arguments) {
                return (double)System.currentTimeMillis() / 1000.0;
            }
            @Override
                public String toString() { return "<native fn>"; }
            });
    }
    /*
     * main interpret method
     */
    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Ruby.runtimeError(error);
        }
        //exception handling for break and next statements
        catch (BreakException breakException) {
            System.out.println(breakException.getMessage());
        }

        catch (NextException nextException) {
            System.out.println(nextException.getMessage());
        }
    }

    /*
     * Statements implemented by visitor
     */
    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
      RubyFunction function = new RubyFunction(stmt,environment);
      environment.define(stmt.name.lexeme, function);
      return null;
    }
    // this function implements the if statement it checks which condition is correct and 
    //implements the branch statements corresponding it
    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        int i = 0;
        for (Expr condition : stmt.conditions) {
            if (isTruth(evaluate(condition))) {
                for (Stmt branch : stmt.branches.get(i)) {
                    execute(branch);
                }
                break;
            }
            i++;
        }
        if (i == stmt.conditions.size() && stmt.elseBranch != null) {
            for (Stmt branch : stmt.elseBranch) {
                execute(branch);
            }
        }
        return null;
    }
    // this function implements the case statement it checks which condition matches with given expression and
    // implements the branch statements corresponding it
    @Override
    public Void visitCaseStmt(Stmt.Case stmt) {
        Object expression = evaluate(stmt.condition);
        int i = 0;
        for (Expr condition : stmt.conditions) {
            if ((evaluate(condition) == expression)
                    || evaluate(condition).toString().compareTo(expression.toString()) == 0) {
                for (Stmt branch : stmt.branches.get(i)) {
                    execute(branch);
                }
                break;
            }
            i++;
        }
        if (i == stmt.conditions.size() && stmt.elseBranch != null) {
            for (Stmt branch : stmt.elseBranch) {
                execute(branch);
            }
        }
        return null;
    }
    //this function implements the visit method for while statement
    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        //as long as the condition is true, it will execute the statements in the while loop's body
        try {
            while (isTruth(evaluate(stmt.condition))) {
                    for (Stmt statement : stmt.body) {
                    try{
                        execute(statement);
                    }
                     catch (NextException nextException){
                          break;
                    }
              }
            }   
        } catch (BreakException breakException) {
            // do nothing just exit
        }
       
        return null;

    }
    //visit method implementation for break statement 
    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        throw new BreakException("Invalid break");

    }
    //visit method implementation for next statement
    @Override
    public Void visitNextStmt(Stmt.Next stmt) {
         throw new NextException("Invalid next");
    }
    //this function implements the visit method for until statement
    //similar to while it will execute the statements in the body of the until loop until the condition becomes true
    @Override
    public Void visitUntilStmt(Stmt.Until stmt) {
        while (!isTruth(evaluate(stmt.condition))) {
            for (Stmt statement : stmt.body) {               
                        execute(statement);           
            }
        }
        return null;
    }
    //this method creates a new environment and executes the statements in the body of the loop
    void executeLoop(List<Stmt> body, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;
            while (true) {
                for (Stmt statement : body) {
                        execute(statement);
                }
            }
        } catch (BreakException breakException) {
            // handle the break stmt
        } finally {
            this.environment = previous;
        }
    }
    //visit method implementation for 'loop' statement 
    public Void visitLoopStmt(Stmt.Loop stmt) {
        executeLoop(stmt.body, new Environment(environment));
        return null;
    }
    // visit method for for statement
    @Override
    public Void visitForStmt(Stmt.For stmt) {
        try{
        // eveluating iterable expression to get the values to iterate over
        Object iterableValue = evaluate(stmt.iterable);

        if (iterableValue instanceof Iterable<?>) {
            for (Object element : (Iterable<?>) iterableValue) {
                 // defining the loop variable in its scope
                 environment.define(stmt.variable.lexeme, element);
                // executing the for each loop statement
                for (Stmt statement : stmt.body) {
                   try{
                        execute(statement);
                    }
                    // implementation of next statement 
                     catch (NextException nextException){
                          break;
                    }
                }
            }
        } else {
            Ruby.runtimeError(new RuntimeError("Cannot have a non iteratable in for loop"));
        }}
        catch(BreakException breakException)
        {
            // do nothing
        }

        return null;
    }
    // this method implement unless statement 
    @Override
    public Void visitUnlessStmt(Stmt.Unless stmt) {
        if (!isTruth(evaluate(stmt.condition))) {
            for (Stmt branch : stmt.branch) {
                execute(branch);
            }
        } else {
            if (stmt.elseBranch != null) {
                for (Stmt branch : stmt.elseBranch) {
                    execute(branch);
                }
            }
        }
        return null;
    }
// this method prints the expression given to and prints according to the colled function(print or puts)
    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        for (Expr expression : stmt.expressions) {
            Object value = evaluate(expression);
            String string = value!=null? stringify(value):"\0";//print null character when null is produced
            if (stmt.type) {
                System.out.println(string);
            } else {
                System.out.print(string);
            }
        }
        return null;
    }
 @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
      Object value = null;
      if (stmt.value != null) value = evaluate(stmt.value);
      throw new Return(value);
    }
    //this method is used to evaluvate the list given to print statement for printing
    @Override
    public Object visitListExpr(Expr.PrintList expr) {
        return evaluate(expr.right);
    }
    // this method is used for parallel assignments like x,y=y,x or x,y,z=10,20,30
    // here we first evaluate the rhs and then assign to variables on lhs correspondingly
    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        int index = 0;
        List<Object> values = new ArrayList<>();
        for (Expr Initializer : stmt.initializer) {
            if (Initializer != null) {
                Object value = evaluate(Initializer);
                values.add(value);
                index++;
            }
        }
        for (int i = 0; i < index; i++) {
            environment.define(stmt.name.get(i).lexeme, values.get(i));
        }

        return null;
    }

    /*
     * Expressions
     */
    // the token already has the value
    //this function implements the visit method for range expression
    //during parsing itself we will decide whether the right value of the operand is inclusive or not depending on the token type
    //then we will execute the statements in the body of the for loop 
    @Override
    public Object visitRangeExpr(Expr.Range expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        if (!(left instanceof Integer) || !(right instanceof Integer)) {
            return null;
        }

        int start = (int) left;
        int end = (int) right;

        List<Object> result = new ArrayList<>();

        for (int i = start; i <= (expr.inclusive ? end : end - 1); i++) {
            result.add(i);
        }

        return result;
    }
    /*
     * implementing visitor methods
     */
    // Literal - just return the value
    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }
    // Grouping expression - evalute the expression and 
    // return the result
    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }
    // Evaluate the operand to a single value first
    // then depending on the operator return the expected value
    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {

            case BANG:
                return !isTruth(right);
            case MINUS:
                if (right instanceof Double)
                    return -(double) right;
                return -(int) right;
        }
        // just to satisfy the jvm
        return null;
    }
    // Evalute the left and right 
    // Now evaluate the expression based on the operator
    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        /*
         * In comparisons and operators like exponent +, and *
         * Multiple types have differnt differnt stuff to be done
         * Therefore checking the type and also type casting as what we 
         * have is type object
         * 
         * Object that is a instance of Integer, needs to be explicity type casted
         * to Integer and only then we can cast it to double
         */
        switch (expr.operator.type) {
            // Exponent
            /*
             * In each place checking if operands are just numbers
             * for all the operators that only work on numbers
             */
            case STAR_STAR:
                checkNumberOperands(expr.operator, left, right);
                if (operandDoubleChecker(left, right)) {
                    if (left instanceof Integer) {
                        return Math.pow((double) (Integer) left, (double) right);
                    }
                    if (right instanceof Integer) {
                        return Math.pow((double) left, (double) (Integer) right);
                    }
                    return Math.pow((double) left, (double) right);
                }
                if (left instanceof Integer && right instanceof Integer) {
                    if ((int) right < 0) {
                        return "1/" + (int) Math.pow((int) left, -(int) right);
                    }
                    return (int) Math.pow((int) left, (int) right);
                }
                // Comparison
            case GREATER:
                if (operandDoubleChecker(left, right)) {
                    if (left instanceof Integer) {
                        return (double) (Integer) left > (double) right;
                    }
                    if (right instanceof Integer) {
                        return (double) left > (double) (Integer) right;
                    }
                    return (double) left > (double) right;
                }
                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left > (int) right;
                }
                if (left instanceof String && right instanceof String) {
                    return true ? ((String) left).compareTo((String) right) > 0 : false;
                }
                throw new RuntimeError(expr.operator, "Operands must be two int/f or two strings.");
            case GREATER_EQUAL:
                if (operandDoubleChecker(left, right)) {
                    if (left instanceof Integer) {
                        return (double) (Integer) left >= (double) right;
                    }
                    if (right instanceof Integer) {
                        return (double) left >= (double) (Integer) right;
                    }
                    return (double) left >= (double) right;
                }
                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left >= (int) right;
                }
                if (left instanceof String && right instanceof String) {
                    return true ? ((String) left).compareTo((String) right) >= 0 : false;
                }
                throw new RuntimeError(expr.operator, "Operands must be two int/f or two strings.");
            case LESS:
                if (operandDoubleChecker(left, right)) {
                    if (left instanceof Integer) {
                        return (double) (Integer) left < (double) right;
                    }
                    if (right instanceof Integer) {
                        return (double) left < (double) (Integer) right;
                    }
                    return (double) left < (double) right;
                }
                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left < (int) right;
                }
                if (left instanceof String && right instanceof String) {
                    return true ? ((String) left).compareTo((String) right) < 0 : false;
                }
                throw new RuntimeError(expr.operator, "Operands must be two int/f or two strings.");
            case LESS_EQUAL:
                if (operandDoubleChecker(left, right)) {
                    if (left instanceof Integer) {
                        return (double) (Integer) left <= (double) right;
                    }
                    if (right instanceof Integer) {
                        return (double) left <= (double) (Integer) right;
                    }
                    return (double) left <= (double) right;
                }
                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left <= (int) right;
                }
                if (left instanceof String && right instanceof String) {
                    return true ? ((String) left).compareTo((String) right) <= 0 : false;
                }
                throw new RuntimeError(expr.operator, "Operands must be two int/f or two strings.");
            /*
             * Just return if it is equal or not equal 
             */
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
            // Operators
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                if (operandDoubleChecker(left, right)) {
                    if (left instanceof Integer) {
                        return (double) (Integer) left - (double) right;
                    }
                    if (right instanceof Integer) {
                        return (double) left - (double) (Integer) right;
                    }
                    return (double) left - (double) right;
                }
                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left - (int) right;
                }
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                if (operandDoubleChecker(left, right)) {
                    if (left instanceof Integer) {
                        return (double) (Integer) left / (double) right;
                    }
                    if (right instanceof Integer) {
                        return (double) left / (double) (Integer) right;
                    }
                    return (double) left / (double) right;
                }
                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left / (int) right;
                }
                // Ruby supports string replication
            case STAR:
                // Float is rounded to int and the replication is done in ruby
                if (left instanceof String && (right instanceof Integer || right instanceof Double)) {
                    return StringReplicator((String) left, (int) right);
                }
                if (operandDoubleChecker(left, right)) {
                    if (left instanceof Integer) {
                        return (double) (Integer) left * (double) right;
                    }
                    if (right instanceof Integer) {
                        return (double) left * (double) (Integer) right;
                    }
                    return (double) left * (double) right;
                }
                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left * (int) right;
                }
                throw new RuntimeError(expr.operator, "Operands must be either String followed by integer or two.");
            case PLUS:
                if (operandDoubleChecker(left, right)) {
                    if (left instanceof Integer) {
                        return (double) (Integer) left + (double) right;
                    }
                    if (right instanceof Integer) {
                        return (double) left + (double) (Integer) right;
                    }
                    return (double) left + (double) right;
                }
                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left + (int) right;
                }
                if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }
                throw new RuntimeError(expr.operator, "Operands must be two int/f or two strings.");
            case MOD:
                if (operandDoubleChecker(left, right)) {
                    if (left instanceof Integer) {
                        return (double) (Integer) left % (double) right;
                    }
                    if (right instanceof Integer) {
                        return (double) left % (double) (Integer) right;
                    }
                    return (double) left % (double) right;
                }
                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left % (int) right;
                }
                throw new RuntimeError(expr.operator, "Operands must be two int/f or two strings.");
        }
        // again to satisy jvm
        return null;
    }

    
    @Override
    /*
     *First, we evaluate the expression for the callee
     * this expression is just an identifier that looks up the function by its name.
     * Then we evaluate each of the argument expressions in order and store the resulting values in a list.
     */
    public Object visitCallExpr(Expr.Call expr) {
      Object callee = evaluate(expr.callee);
      List<Object> arguments = new ArrayList<>();
      for (Expr argument : expr.arguments) {
        arguments.add(evaluate(argument));
      }
      if (!(callee instanceof RubyCallable)) {
        throw new RuntimeError(expr.paren,
            "Can only call functions and classes.");
      }
      RubyCallable function = (RubyCallable)callee;
      //we check to see if the argument list’s length matches the callable’s arity.
      if (arguments.size() != function.arity()) {
        throw new RuntimeError(expr.paren, "Expected " +
            function.arity() + " arguments but got " +
            arguments.size() + ".");
      }
      return function.call(this, arguments);
    }
    /*
     * Method to check if any of the number operands are double
     * if yes we need to typecast
     */
    private boolean operandDoubleChecker(Object left, Object right) {
        if (left instanceof Double && right instanceof Double)
            return true;
        if (left instanceof Double && right instanceof Integer) {
            right = (Integer) right;
            return true;
        }
        if (left instanceof Integer && right instanceof Double) {
            left = (Integer) left;
            return true;
        }
        return false;
    }
    /*
     * Check if the operands are numbers
     * throw error if not
     */
    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (operandDoubleChecker(left, right) || left instanceof Integer && right instanceof Integer)
            return;
        throw new RuntimeError(operator, "Operators must be numbers." + right + left);
    }
    // this method is for unary operator and checks if it operating on numbers else it throws errors
    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double || operand instanceof Integer)
            return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }
    //checks the instances of the objects and returns if they are equal are not based on it
    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null)
            return true;
        if (a == null)
            return false;
        if (a instanceof Integer && b instanceof Double)
            return (double) (Integer) a == (double) b;
        if (b instanceof Integer && a instanceof Double)
            return (double) (Integer) b == (double) a;
        return a.equals(b);
    }
    //this method is used replicate string as string multiplcation is allowed in ruby
    private String StringReplicator(String str, int count) {
        String str1 = "";
        for (int i = 0; i < count; i++) {
            str1 += str;
        }
        return str1;
    }
    // this is method implements vistor pattern which is used to classify to which expression belongs to
    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    // everything other than null and false is true in ruby
    private boolean isTruth(Object object) {
        if (object == null)
            return false;
        if (object instanceof Boolean)
            return (boolean) object;
        return true;
    }
    // this method is used to convert objects to strings for printing
    private String stringify(Object object) {
        if (object == null)
            return "nil";
        return object.toString();
    }
    // this is method implements vistor pattern which is used to classify to which statement belongs to
    private void execute(Stmt stmt) {
        stmt.accept(this);
    }
    // this methods creates a new scope and executes the statement in block in new scope
    void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }
    // this used to call executeBlock as it is must be implemented by vistors pattern
    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }
    //this method returns the value of varible given in coide
    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }
    //this method is used to implement 'and' and 'or' operators
    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isTruth(left))
                return left;
        } else {
            if (!isTruth(left))
                return left;
        }

        return evaluate(expr.right);
    }
    //this method is used for assigments majorly
    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object left = (expr.operator.type == EQUAL) ? expr.name.lexeme : environment.get(expr.name);
        Object right = evaluate(expr.value);
        switch (expr.operator.type) {
 // when operator is equal to it evalute and assign variables example a=(b=(c=5)+2)+10 or a=10 and
 // return value so we can assign for other varibles
            case EQUAL:
                environment.define(left.toString(), right);
                return right;
            // when +=,-=,*=,/=,%= we evalaute the expression and assign the value obtained on varible which it is used and 
            //return null 
            case PLUS_EQUAL:
                if (operandDoubleChecker(left, right)) {
                    if (left instanceof Integer) {
                        Object value = (double) (Integer) left + (double) right;
                        environment.assign(expr.name, value);
                    }
                    if (right instanceof Integer) {
                        Object value = (double) left + (double) (Integer) right;
                        environment.assign(expr.name, value);
                    }
                    Object value = (double) left + (double) right;
                    environment.assign(expr.name, value);
                }
                if (left instanceof Integer && right instanceof Integer) {
                    Object value = (int) left + (int) right;
                    environment.assign(expr.name, value);
                }
                if (left instanceof String && right instanceof String) {
                    Object value = (String) left + (String) right;
                    environment.assign(expr.name, value);
                }
                break;
            case MINUS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                if (operandDoubleChecker(left, right)) {
                    if (left instanceof Integer) {
                        Object value = (double) (Integer) left - (double) right;
                        environment.assign(expr.name, value);
                    }
                    if (right instanceof Integer) {
                        Object value = (double) left - (double) (Integer) right;
                        environment.assign(expr.name, value);
                    }
                    Object value = (double) left - (double) right;
                    environment.assign(expr.name, value);
                }
                if (left instanceof Integer && right instanceof Integer) {
                    Object value = (int) left - (int) right;
                    environment.assign(expr.name, value);
                }
                break;
            case STAR_EQUAL:
                if (left instanceof String && (right instanceof Integer || right instanceof Double)) {
                    Object value = StringReplicator((String) left, ((int) right));
                    environment.assign(expr.name, value);
                }
                if (operandDoubleChecker(left, right)) {
                    if (left instanceof Integer) {
                        Object value = (double) (Integer) left * (double) right;
                        environment.assign(expr.name, value);
                    }
                    if (right instanceof Integer) {
                        Object value = (double) left * (double) (Integer) right;
                    }
                    Object value = (double) left * (double) right;
                    environment.assign(expr.name, value);
                }
                if (left instanceof Integer && right instanceof Integer) {
                    Object value = (int) left * (int) right;
                    environment.assign(expr.name, value);
                }
                break;
            case SLASH_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                if (operandDoubleChecker(left, right)) {
                    if (left instanceof Integer) {
                        Object value = (double) (Integer) left / (double) right;
                        environment.assign(expr.name, value);
                    }
                    if (right instanceof Integer) {
                        Object value = (double) left / (double) (Integer) right;
                        environment.assign(expr.name, value);
                    }
                    Object value = (double) left / (double) right;
                    environment.assign(expr.name, value);
                }
                if (left instanceof Integer && right instanceof Integer) {
                    Object value = (int) left / (int) right;
                    environment.assign(expr.name, value);
                }
                break;
            case MOD_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                if (operandDoubleChecker(left, right)) {
                    if (left instanceof Integer) {
                        Object value = (double) (Integer) left % (double) right;
                        environment.assign(expr.name, value);
                    }
                    if (right instanceof Integer) {
                        Object value = (double) left % (double) (Integer) right;
                        environment.assign(expr.name, value);
                    }
                    Object value = (double) left % (double) right;
                    environment.assign(expr.name, value);
                }
                if (left instanceof Integer && right instanceof Integer) {
                    Object value = (int) left % (int) right;
                    environment.assign(expr.name, value);
                }
                break;
            default:
        }

        return null;
    }

}
