package ruby;

import java.util.List;

abstract class Stmt {
	interface Visitor<R> {
		R visitBlockStmt(Block stmt);

		R visitExpressionStmt(Expression stmt);

		R visitIfStmt(If stmt);

		R visitPrintStmt(Print stmt);

		R visitVarStmt(Var stmt);

		R visitPutsStmt(Puts stmt);

		R visitUnlessStmt(Unless stmt);

		R visitWhileStmt(While stmt);
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
        While(Expr condition, List<Stmt> body)
		{
			this.condition = condition;
			this.body = body;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitWhileStmt(this);
		}
        final Expr condition;
		final  List<Stmt> body;
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
		Print(List<Expr> expressions) {
			this.expressions = expressions;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitPrintStmt(this);
		}

		final List<Expr> expressions;
	}

	static class Puts extends Stmt {
		Puts(List<Expr> expressions) {
			this.expressions = expressions;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitPutsStmt(this);
		}

		final List<Expr> expressions;
	}

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
