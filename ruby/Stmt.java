package ruby;

import java.util.List;
// abstract class for implementing different types statements in ruby AST.
abstract class Stmt {
    // interface visitor for implementing visitors pattern.
	interface Visitor<R> {
		R visitBlockStmt(Block stmt);

		R visitExpressionStmt(Expression stmt);

		R visitIfStmt(If stmt);

		R visitPrintStmt(Print stmt);

		R visitVarStmt(Var stmt);

		R visitUnlessStmt(Unless stmt);

		R visitWhileStmt(While stmt);

		R visitUntilStmt(Until stmt);

		R visitBreakStmt(Break stmt);

		R visitForStmt(For stmt);

		R visitLoopStmt(Loop stmt);

		R visitCaseStmt(Case stmt);

		R visitFunctionStmt(Function stmt);

		R visitReturnStmt(Return stmt);

		R visitNextStmt(Next stmt);
	}
    // case statement implementation
	static class Case extends Stmt {
		Case(Expr condition, List<Expr> conditions, List<List<Stmt>> branches, List<Stmt> elseBranch) {
			this.condition = condition;
			this.conditions = conditions;
			this.branches = branches;
			this.elseBranch = elseBranch;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitCaseStmt(this);
		}

		final Expr condition;
		final List<Expr> conditions;
		final List<List<Stmt>> branches;
		final List<Stmt> elseBranch;
	}
    // break statement implementation
	static class Break extends Stmt {
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitBreakStmt(this);
		}
	}
    // next statement implementation
	static class Next extends Stmt {
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitNextStmt(this);
		}
	}
    // block statement implementation
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
    // expression statement implementation
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
    // if statement implementation
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
    // while statement implementation
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
    // until statement implementation
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
    // loop statement implementation
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
     // for statement implementation
	 static class For extends Stmt {
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
    // unless statement implementation
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
    // function statement implementation
	static class Function extends Stmt {
		Function(Token name, List<Token> params, List<Stmt> body) {
			this.name = name;
			this.params = params;
			this.body = body;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitFunctionStmt(this);
		}
		final Token name;
		final List<Token> params;
		final List<Stmt> body;
	}
    // print statement implementation
	static class Print extends Stmt {
		Print(List<Expr> expressions, boolean type) {
			this.expressions = expressions;
			this.type = type;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitPrintStmt(this);
		}

		final List<Expr> expressions;
		final boolean type;
	}
    // return statement implementation
	static class Return extends Stmt {
		Return(Token keyword, Expr value) {
			this.keyword = keyword;
			this.value = value;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitReturnStmt(this);
		}
		final Token keyword;
		final Expr value;
	}
    // variable statement implementation
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
