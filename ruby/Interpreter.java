package ruby;

import static ruby.TokenType.EQUAL;

import java.util.ArrayList;
import java.util.List;
import java.lang.Math;
//import ruby.Expr.Variable;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    private static class BreakException extends RuntimeException {
        BreakException(String message) {
            super(message);
        }
    }

    private Environment environment = new Environment();

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Ruby.runtimeError(error);
        }
        // exception for the break statement should also be handled here too
        catch (BreakException breakException) {
            System.out.println(breakException.getMessage());
        }
    }

    /*
     * Statements
     */
    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        // if (isTruthy(evaluate(stmt.condition))) {
        // execute(stmt.thenBranch);
        // } else if (stmt.elseBranch != null) {
        // execute(stmt.elseBranch);
        // }
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

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        try {
            while (isTruth(evaluate(stmt.condition))) {
                for (Stmt statement : stmt.body) {
                    execute(statement);
                }
            }
        } catch (BreakException breakException) {
            // handle the break stmt
        }
        return null;

    }

    public Void visitBreakStmt(Stmt.Break stmt) {
        throw new BreakException("Invalid break");

    }

    public Void visitUntilStmt(Stmt.Until stmt) {
        while (!isTruth(evaluate(stmt.condition))) {
            for (Stmt statement : stmt.body) {
                execute(statement);
            }
        }
        return null;
    }

    void executeLoop(List<Stmt> body, Environment environment) {
        Environment previous = this.environment;
        System.out.println("changed to new");
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
            System.out.println("changed to previous");
        }
    }

    public Void visitLoopStmt(Stmt.Loop stmt) {
        executeLoop(stmt.body, new Environment(environment));
        return null;
    }

    @Override
    public Void visitForStmt(Stmt.For stmt) {
        try{
        Object iterableValue = evaluate(stmt.iterable);

        if (iterableValue instanceof Iterable<?>) {
            for (Object element : (Iterable<?>) iterableValue) {
               // environment.define(left.toString(), right);
               //System.out.println(element);
                // Create a new environment for the loop iteration
                 environment.define(stmt.variable.lexeme, element);

                // Execute the loop body with the new environment
                // execute(stmt.body);
                for (Stmt statement : stmt.body) {
                    execute(statement);
                }
            }
        } else {
            // Handle error: Non-iterable in the for loop
        }}
        catch(BreakException breakException)
        {
            // have to add
        }

        return null;
    }

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

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        // Object value = evaluate(stmt.expressions);
        // String string = stringify(value);
        // String -->Object --> String seems to mess all escape sequence characters
        // So this is needed

        // String finalString = string.replace("\\n", "\n"); // change this for proper
        // output

        /*
         * String[] stringArray = string.split("\n");
         * System.out.println(stringArray[1]);
         * int arrayLength = stringArray.length;
         * int index=0;
         * while(index<arrayLength-1){
         * System.out.println(stringArray);
         * index++;
         * }
         */

        for (Expr expression : stmt.expressions) {
            Object value = evaluate(expression);
            String string = stringify(value);
            if (stmt.type) {
                System.out.println(string);
            } else {
                System.out.print(string);
            }
        }
        return null;
    }

    // @Override
    // public Void visitPutsStmt(Stmt.Puts stmt) {
    // for (Expr expression : stmt.expressions) {
    // Object value = evaluate(expression);
    // String string = stringify(value);
    // System.out.println(string);
    // }
    // return null;
    // }

    @Override
    public Object visitListExpr(Expr.PrintList expr) {
        return evaluate(expr.right);
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        int index = 0;
        // System.out.println("asdfgh");
        List<Object> values = new ArrayList<>();
        for (Expr Initializer : stmt.initializer) {
            if (Initializer != null) {
                Object value = evaluate(Initializer);
                values.add(value);
                // System.out.println(value + "aaa");
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
    @Override
    public Object visitRangeExpr(Expr.Range expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        if (!(left instanceof Integer) || !(right instanceof Integer)) {
            // Handle error: Non-integer range boundaries
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

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

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

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            // Exponent
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

            // case DOT_DOT:
            // return rangeDotDot(left, right);
            // case DOT_DOT_DOT:
            // return rangeDotDotDot(left, right);
        }
        // again to satisy jvm
        return null;
    }

    /*
     * Helper Methods
     */
    // private List<Object> rangeDotDot(Object left, Object right) {
    // if (left instanceof Integer && right instanceof Integer) {
    // int start = (int) left;
    // int end = (int) right;

    // List<Object> result = new ArrayList<>();
    // for (int i = start; i <= end; i++) {
    // result.add(i);
    // }
    // return result;
    // } else {
    // // Handle error: Non-integer range boundaries
    // return null;
    // }
    // }
    // private List<Object> rangeDotDotDot(Object left, Object right) {
    // if (left instanceof Integer && right instanceof Integer) {
    // int start = (int) left;
    // int end = (int) right;

    // List<Object> result = new ArrayList<>();
    // for (int i = start; i < end; i++) {
    // result.add(i);
    // }
    // return result;
    // } else {
    // // Handle error: Non-integer range boundaries
    // return null;
    // }
    // }

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

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (operandDoubleChecker(left, right) || left instanceof Integer && right instanceof Integer)
            return;
        throw new RuntimeError(operator, "Operators must be numbers." + right + left);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double || operand instanceof Integer)
            return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

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

    private String StringReplicator(String str, int count) {
        String str1 = "";
        for (int i = 0; i < count; i++) {
            str1 += str;
        }
        return str1;
    }

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

    private String stringify(Object object) {
        if (object == null)
            return "nil";
        return object.toString();
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

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

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

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

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object left = (expr.operator.type == EQUAL) ? expr.name.lexeme : environment.get(expr.name);
        Object right = evaluate(expr.value);
        switch (expr.operator.type) {
            case EQUAL:
                environment.define(left.toString(), right);
                return right;
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
