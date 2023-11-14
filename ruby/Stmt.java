package ruby;

import static ruby.TokenType.PUTS;

import java.util.List;

abstract class Stmt {
	interface Visitor<R> {
		R visitBlockStmt(Block stmt);

		R visitExpressionStmt(Expression stmt);

		R visitIfStmt(If stmt);

		R visitPrintStmt(Print stmt);

		R visitVarStmt(Var stmt);

		// R visitPutsStmt(Puts stmt);

		R visitUnlessStmt(Unless stmt);

		R visitWhileStmt(While stmt);

		R visitUntilStmt(Until stmt);

		R visitBreakStmt(Break stmt);

		R visitForStmt(For stmt);

		R visitLoopStmt(Loop stmt);
	}

	static class Break extends Stmt {
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitBreakStmt(this);
		}
	}

	static class Block extends Stmt {
		Block(List<Stmt> statements) {
			this.statements = statements;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitBlockStmt(this);
		}

		final List<Stmt> statements;
	}

	static class Expression extends Stmt {
		Expression(Expr expression) {
			this.expression = expression;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitExpressionStmt(this);
		}

		final Expr expression;
	}

	static class If extends Stmt {
		If(List<Expr> conditions, List<List<Stmt>> branches, List<Stmt> elseBranch) {
			this.conditions = conditions;
			this.branches = branches;
			this.elseBranch = elseBranch;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitIfStmt(this);
		}

		final List<Expr> conditions;
		final List<List<Stmt>> branches;
		final List<Stmt> elseBranch;
	}

	static class While extends Stmt {
		While(Expr condition, List<Stmt> body) {
			this.condition = condition;
			this.body = body;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitWhileStmt(this);
		}

		final Expr condition;
		final List<Stmt> body;
	}

	static class Until extends Stmt {
		Until(Expr condition, List<Stmt> body) {
			this.condition = condition;
			this.body = body;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitUntilStmt(this);
		}

		final Expr condition;
		final List<Stmt> body;
	}

	static class Loop extends Stmt {
		Loop(List<Stmt> body) {
			this.body = body;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitLoopStmt(this);
		}

		final List<Stmt> body;
	}

	public static class For extends Stmt {
		public final Token variable;
		public final Expr iterable;
		public final List<Stmt> body;

		public For(Token variable, Expr iterable, List<Stmt> body) {
			this.variable = variable;
			this.iterable = iterable;
			this.body = body;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitForStmt(this);
		}
	}

	static class Unless extends Stmt {
		Unless(Expr condition, List<Stmt> branch, List<Stmt> elseBranch) {
			this.condition = condition;
			this.branch = branch;
			this.elseBranch = elseBranch;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitUnlessStmt(this);
		}

		final Expr condition;
		final List<Stmt> branch;
		final List<Stmt> elseBranch;
	}

	static class Print extends Stmt {
		Print(List<Expr> expressions, TokenType token) {
			this.expressions = expressions;
			this.type = (token == PUTS) ? true : false;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitPrintStmt(this);
		}

		final List<Expr> expressions;
		final boolean type;
	}

	// static class Puts extends Stmt {
	// Puts(List<Expr> expressions) {
	// this.expressions = expressions;
	// }

	// @Override
	// <R> R accept(Visitor<R> visitor) {
	// return visitor.visitPutsStmt(this);
	// }

	// final List<Expr> expressions;
	// }

	static class Var extends Stmt {
		Var(List<Token> name, List<Expr> initializer) {
			this.name = name;
			this.initializer = initializer;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitVarStmt(this);
		}

		final List<Token> name;
		final List<Expr> initializer;
	}

	abstract <R> R accept(Visitor<R> visitor);
}
