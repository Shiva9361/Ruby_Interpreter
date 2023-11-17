package ruby;

import java.util.List;
// Abstract base class for implementing  expression types in Ruby AST
abstract class Expr {
    // interface visitor for implementing visitors pattern.
	interface Visitor<R> {
		R visitAssignExpr(Assign expr);

		R visitBinaryExpr(Binary expr);

		R visitGroupingExpr(Grouping expr);

		R visitLiteralExpr(Literal expr);

		R visitUnaryExpr(Unary expr);

		R visitVariableExpr(Variable expr);

		R visitListExpr(PrintList expr);

		R visitLogicalExpr(Logical expr);

		R visitRangeExpr(Expr.Range expr);
		
		R visitCallExpr(Call expr);
	}
    // range expression implementation
	public static class Range extends Expr {
		public final Expr left;
		public final Expr right;
		public final boolean inclusive;

		public Range(Expr left, Expr right, boolean inclusive) {
			this.left = left;
			this.right = right;
			this.inclusive = inclusive;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitRangeExpr(this);
		}
	}
    // assignment expression implementation
	static class Assign extends Expr {
		Assign(Token name, Token operator, Expr value) {
			this.name = name;
			this.operator = operator;
			this.value = value;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitAssignExpr(this);
		}

		final Token name;
		final Token operator;
		final Expr value;
	}
    // printlist expression implementation
	static class PrintList extends Expr {
		PrintList(Expr left, Expr right) {
			this.left = left;
			this.right = right;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitListExpr(this);
		}

		final Expr left;
		final Expr right;
	}
    // binary expression implementation
	static class Binary extends Expr {
		Binary(Expr left, Token operator, Expr right) {
			this.left = left;
			this.operator = operator;
			this.right = right;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitBinaryExpr(this);
		}

		final Expr left;
		final Token operator;
		final Expr right;
	}
    // grouping expression implementation
	static class Grouping extends Expr {
		Grouping(Expr expression) {
			this.expression = expression;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitGroupingExpr(this);
		}

		final Expr expression;	
	}
    // literal expression implementation
	static class Literal extends Expr {
		Literal(Object value) {
			this.value = value;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitLiteralExpr(this);
		}

		final Object value;
	}
    // unary expression implementation
	static class Unary extends Expr {
		Unary(Token operator, Expr right) {
			this.operator = operator;
			this.right = right;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitUnaryExpr(this);
		}

		final Token operator;
		final Expr right;
	}
    // logical expression implementation
	static class Logical extends Expr {
		Logical(Expr left, Token operator, Expr right) {
			this.left = left;
			this.operator = operator;
			this.right = right;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitLogicalExpr(this);
		}

		final Expr left;
		final Token operator;
		final Expr right;
	}
    // function call expression implementation
	static class Call extends Expr {
		final Expr callee;
		final Token paren;
		final List<Expr> arguments ;

		Call(Expr callee, Token paren, List<Expr> arguments) {
			this.callee = callee;
			this.paren = paren;
			this.arguments = arguments;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitCallExpr(this);
		}
	}
    // variable declaration expression implementation	
	static class Variable extends Expr {
		Variable(Token name) {
			this.name = name;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitVariableExpr(this);
		}

		final Token name;
	}

	abstract <R> R accept(Visitor<R> visitor);
}
