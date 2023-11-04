package ruby;

import java.util.List;

import ruby.Expr.Variable;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    private Environment environment = new Environment();

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Ruby.runtimeError(error);
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
            System.out.print(string);
        }
        return null;
    }

    @Override
    public Void visitPutsStmt(Stmt.Puts stmt) {
        for (Expr expression : stmt.expressions) {
            Object value = evaluate(expression);
            String string = stringify(value);
            System.out.println(string);
        }
        return null;
    }

    @Override
    public Object visitListExpr(Expr.List expr) {
        return evaluate(expr.right);
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        int index = 0;
        // System.out.println("asdfgh");
        for (Expr Initializer : stmt.initializer) {
            if (Initializer != null) {
                Object value = evaluate(Initializer);
                environment.define(stmt.name.get(index).lexeme, value);
                // System.out.println(value + "aaa");
                index++;
            }
        }

        return null;
    }

    /*
     * Expressions
     */
    // the token already has the value
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
                return -(double) right;
        }
        // just to satisfy the jvm
        return null;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            // Comparison
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left >= (double) right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left <= (double) right;

            case BANG_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                checkNumberOperands(expr.operator, left, right);
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
        }
        // again to satisy jvm
        return null;
    }

    /*
     * Helper Methods
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
        return a.equals(b);
    }

    private String StringReplicator(String str, int count) {
        for (int i = 0; i < count; i++)
            str += str;
        return str;
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

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

}
