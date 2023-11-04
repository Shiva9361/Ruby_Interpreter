package ruby;

import java.util.ArrayList;
import java.util.List;
import static ruby.TokenType.*;

public class Parser {
  private static class ParseError extends RuntimeException {
  }

  private final List<Token> tokens;
  private int current = 0;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  List<Stmt> parse() {
    List<Stmt> statements = new ArrayList<>();
    while (!isAtEnd()) {
      statements.add(declaration());
    }
    return statements;
  }

  /*
   * Expressions
   */
  private Expr expression() {
    return assignment();
  }

  private List<Expr> expressionList() {
    List<Expr> exprs = new ArrayList<>();
    Expr expr = expression();
    exprs.add(expr);
    while (match(COMMA)) {
      Expr right = expression();
      exprs.add(right);
      expr = new Expr.List(expr, right);
    }

    return exprs;
  }

  private Expr equality() {
    Expr expr = comparison();
    while (match(BANG_EQUAL, EQUAL_EQUAL)) {
      Token operator = previous();
      Expr right = comparison();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  private Expr comparison() {
    Expr expr = term();
    while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
      Token operator = previous();
      Expr right = term();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  private Expr term() {
    Expr expr = factor();
    while (match(MINUS, PLUS)) {
      Token operator = previous();
      Expr right = factor();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  private Expr factor() {
    Expr expr = unary();
    while (match(SLASH, STAR)) {
      Token operator = previous();
      Expr right = unary();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  private Expr unary() {
    if (match(BANG, MINUS)) {
      Token operator = previous();
      Expr right = unary();
      return new Expr.Unary(operator, right);
    }
    return primary();
  }

  private Expr primary() {
    if (match(FALSE))
      return new Expr.Literal(false);
    if (match(TRUE))
      return new Expr.Literal(true);
    if (match(NIL))
      return new Expr.Literal(null);
    if (match(INTEGER, FLOAT, STRING)) {
      return new Expr.Literal(previous().literal);
    }
    if (match(IDENTIFIER)) {
      return new Expr.Variable(previous());
    }
    if (match(LEFT_PAREN)) {
      Expr expr = expression();
      consume(RIGHT_PAREN, "Expect ')' after expression.");
      return new Expr.Grouping(expr);
    }
    throw error(peek(), "Expect expression.");
  }

  /*
   * Statements
   */
  private Stmt declaration() {
    try {
      if (peek().type == IDENTIFIER)
        return varDeclaration();
      return statement();
    } catch (ParseError error) {
      synchronize();
      return null;
    }
  }

  // Free floating identifiers must be handled `
  private Stmt varDeclaration() {
    List<Token> name = new ArrayList<>();
    while (!match(EQUAL)) {
      if (match(COMMA)) {

      }
      Token varibleName = consume(IDENTIFIER, "??");
      System.out.println(varibleName.lexeme);
      name.add(varibleName);
    }

    List<Expr> initializer = new ArrayList<>();

    initializer = expressionList();

    System.out.println(peek().type);
    consume(NEWLINE, "Expect newline after value.");
    if (name.size() != initializer.size()) {
      throw new RuntimeError(null, "insufficient arguments");
    }
    return new Stmt.Var(name, initializer);
  }

  private Stmt statement() {
    if (match(PRINT))
      return printStatement();
    if (match(PUTS))
      return putsStatement();
    return expressionStatement();
  }

  private Stmt printStatement() {
    List<Expr> value = expressionList();
    consume(NEWLINE, "Expect newline after value.");
    return new Stmt.Print(value);
  }

  private Stmt putsStatement() {
    List<Expr> value = expressionList();
    consume(NEWLINE, "Expect newline after value.");
    return new Stmt.Puts(value);
  }

  private Stmt expressionStatement() {
    Expr expr = expression();
    consume(NEWLINE, "Expect newline after value.");
    return new Stmt.Expression(expr);
  }

  private Expr assignment() {
    Expr expr = equality();

    if (match(EQUAL)) {
      Token equals = previous();
      Expr value = assignment();

      if (expr instanceof Expr.Variable) {
        Token name = ((Expr.Variable) expr).name;
        return new Expr.Assign(name, value);
      }

      error(equals, "Invalid assignment target.");
    }

    return expr;
  }

  /*
   * Helper Methods
   */
  // Matches the given token and if it the token then advance
  private boolean match(TokenType... types) {
    for (TokenType type : types) {
      if (check(type)) {
        advance();
        return true;
      }
    }
    return false;
  }

  private Token consume(TokenType type, String message) {
    if (check(type))
      return advance();
    throw error(peek(), message);
  }

  private boolean check(TokenType type) {
    if (isAtEnd())
      return false;
    return peek().type == type;
  }

  private Token advance() {
    if (!isAtEnd())
      current++;
    return previous();
  }

  private boolean isAtEnd() {
    return peek().type == EOF;
  }

  private Token peek() {
    return tokens.get(current); // returns without incrementing
  }

  private Token previous() {
    return tokens.get(current - 1);
  }

  private ParseError error(Token token, String message) {
    Ruby.error(token, message);
    return new ParseError();
  }

  private void synchronize() {
    advance();
    while (!isAtEnd()) {
      if (previous().type == SEMICOLON)
        return;
      switch (peek().type) {
        case CLASS:
          // case FUN:
          // case VAR:
        case FOR:
        case IF:
        case WHILE:
        case PRINT:
        case RETURN:
          return;
      }
      advance();
    }
  }

}