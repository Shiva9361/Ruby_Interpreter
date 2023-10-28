package ruby;
/*
 * A printer to see if the syntax tree is being generated 
 * properly
 * has to bee updated constantly with all the methods 
 * when parser in created
 */
public class AstPrinter implements Expr.Visitor<String> {
    
    String print(Expr expr){
        return expr.accept(this);
    }
    /*
     * Now implemeting all the visitor methods
     */
    // I just like using @Override now lol
    @Override
    public String visitBinaryExpr(Expr.Binary expr){
        return parenthesize(expr.operator.lexeme,expr.left, expr.right);
    }
    
    @Override
    public String visitGroupingExpr(Expr.Grouping expr){
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr){
        //  null is nil in ruby
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }
    @Override
    public String visitUnaryExpr(Expr.Unary expr){
        return parenthesize(expr.operator.lexeme,expr.right);
    }

    /*
     * Helper method
     */

    private String parenthesize(String name, Expr... exprs){
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(name);
        for (Expr expr :exprs){
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }
}
