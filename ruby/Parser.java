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
      expr = new Expr.PrintList(expr, right);
    }

    return exprs;
  }

  private List<Stmt> statementList() {
    List<Stmt> statements = new ArrayList<>();
    do {
      Stmt statement = statement();
      statements.add(statement);
    } while (!(match(END, ELSIF, ELSE)));
    current--;
    return statements;
  }

  private List<Stmt> statementList2() {
    List<Stmt> statements = new ArrayList<>();
    do {
      Stmt statement = statement();
      statements.add(statement);
    } while (!(match(END, WHEN, ELSE)));
    current--;
    return statements;
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
    while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL, DOT_DOT, DOT_DOT_DOT)) {
      Token operator = previous();
      Expr right = term();
      if (operator.type == DOT_DOT_DOT) {
        expr = new Expr.Range(expr, right, false);
      } 
      else if (operator.type == DOT_DOT) {
        expr = new Expr.Range(expr, right, true);
      } 
      else {
        expr = new Expr.Binary(expr, operator, right);
      }
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
    Expr expr = power();
    while (match(SLASH, STAR, MOD)) {
      Token operator = previous();
      Expr right = power();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  private Expr power() {
    Expr expr = unary();
    while (match(STAR_STAR)) {
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
    return call();
  }

// Upper Limit Of Arguments is 255
private Expr finishCall(Expr callee) {
  List<Expr> arguments = new ArrayList<>();
  if (!check(RIGHT_PAREN)) {
    do {
      if (arguments.size() >= 255) {
        error(peek(), "Can't have more than 255 arguments.");
      }
      arguments.add(expression());
    } while (match(COMMA));
  }
  Token paren = consume(RIGHT_PAREN,"Expect ')' after arguments.");
  return new Expr.Call(callee, paren, arguments);
}


private Expr call() {
  Expr expr = primary();
  while (true) {
    if (match(LEFT_PAREN)) {
      expr = finishCall(expr);
    } else {
      break;
    } 
  }
  return expr;
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
    // dealing with empty lines making an empty line as just a null statement
    if ((peek().type == NEWLINE)) {
      return new Expr.Literal(null);
    }

    throw error(peek(), "Expect expression.");
  }

  /*
   * Statements
   */
  private Stmt declaration() {
    try {
      if (match(DEF)) return function("function");
      if (peek().type == IDENTIFIER) {
        if (superPeek().type == COMMA) {// this condition is for checking if it is
                                        // declration or assignment
          return varDeclaration();
        }
      }
      return statement();
    } catch (ParseError error) {
      synchronize();
      return null;
    }
  }

  // this nmethod id used for parallel assignment parses variables to one list and target to another list
  private Stmt varDeclaration() {
    List<Token> name = new ArrayList<>();
    while (!match(EQUAL)) {
      if (match(COMMA)) {

      }
      Token varibleName = consume(IDENTIFIER, "??");
      name.add(varibleName);
    }

    List<Expr> initializer = new ArrayList<>();

    initializer = expressionList();

    consume(NEWLINE, "Expect newline after value.");
    if (name.size() != initializer.size()) {
      throw new RuntimeError(null, "insufficient arguments");
    }
    return new Stmt.Var(name, initializer);
  }

  private Stmt statement() {
    if (match(UNLESS))
      return unlessStatement();
    if (match(IF)) {
      return ifStatement();
    }
    if (match(PRINT, PUTS))
      return printStatement(previous().type);
    // if (match(PUTS))
    // return putsStatement();
    if (match(BEGIN))
      return new Stmt.Block(block());
    if (match(WHILE)) {
      return whileStatement();
    }
    if (match(UNTIL))
      return untilStatement();
    if (match(RETURN)) return returnStatement();
    if (match(BREAK)) {
      return breakStatement();
    }
    if (match(LOOP)) {
      return loopStatement();
    }
    if (match(FOR)) {
      return forStatement();
    }
    if (match(CASE)) {
      return caseStatement();
    }
    if (match(NEXT)) {
      return nextStatement();
    }
    return expressionStatement();
  }

  private Stmt breakStatement() {
    return new Stmt.Break();
  }

  private Stmt nextStatement()
  {
    return new Stmt.Next();
  }
// this method implements unless statement , pareses branches and condition and checks else branch
  private Stmt unlessStatement() {
    Expr condition = expression();
    List<Stmt> branch = new ArrayList<>();
    if (match(THEN)) {
    }
    advance();
    branch = statementList();
    List<Stmt> elseBranch = null;
    if (match(ELSE)) {
      advance();
      elseBranch = statementList();
    }
    consume(END, "expect end keyword");
    return new Stmt.Unless(condition, branch, elseBranch);
  }
// this method implements if statement, this method parses the tokens into condition,and branches using list 
  private Stmt ifStatement() {
    List<Expr> conditions = new ArrayList<>();
    List<List<Stmt>> branches = new ArrayList<>();
    Expr condition = expression();
    if (peek().type == DO) {
      // throws error when do is accidentally used 
      Ruby.error(peek().line, "syntax error ,unexpected " + peek().type);
    }
    if (match(THEN)) {// optional syntax
    }
    advance();
    conditions.add(condition);
    List<Stmt> branch = statementList();
    branches.add(branch);
    while (match(ELSIF)) {
      Expr Condition = expression();
      conditions.add(Condition);
      if (match(THEN)) {
      }
      advance();
      List<Stmt> Branch = statementList();
      branches.add(Branch);
    }
    List<Stmt> elseBranch = null;
    if (match(ELSE)) {
      advance();
      elseBranch = statementList();
    }
    consume(END, "expect end keyword");
    return new Stmt.If(conditions, branches, elseBranch);
  }
// this method first parses the expression of case and them lists of conditions and branches
  private Stmt caseStatement() {
    List<Expr> conditions = new ArrayList<>();
    List<List<Stmt>> branches = new ArrayList<>();

    Expr condition = expression();
    while (!match(WHEN)) {
      if (peek().type == NEWLINE) {
        advance();
      } else {
        Ruby.error(peek().line, "expecting 'when' ");
        break;
      }
    }
    current--;
    while (match(WHEN)) {
      Expr Condition = expression();
      conditions.add(Condition);
      if (match(THEN)) {
      }
      advance();
      List<Stmt> Branch = statementList2();
      branches.add(Branch);
    }
    List<Stmt> elseBranch = null;
    if (match(ELSE)) {
      advance();
      elseBranch = statementList2();
    }
    consume(END, "expect end keyword");
    return new Stmt.Case(condition, conditions, branches, elseBranch);
  }

  private Stmt whileStatement() {
    Expr condition = expression();
    List<Stmt> body = statementList();
    consume(END, "expect end keyword");
    return new Stmt.While(condition, body);
  }

  private Stmt untilStatement() {
    Expr condition = expression();
    consume(DO, "expect do keyword");
    List<Stmt> body = statementList();
    consume(END, "expect end keyword");
    return new Stmt.Until(condition, body);
  }

  private Stmt loopStatement() {
    consume(DO, "expect do keyword");
    List<Stmt> body = statementList();
    consume(END, "expect end keyword");
    return new Stmt.Loop(body);
  }

  private Stmt forStatement() {
    try {
      if (match(IDENTIFIER)) {
        Token variable = previous();
        if (match(IN)) {
          Expr iterable = expression();
          // consume(DO, "Expect 'do' after for statement.");
          List<Stmt> body = statementList();
          consume(END, "Expect 'end' after for block.");
          return new Stmt.For(variable, iterable, body);
        }
      }
      return statement();
    } catch (ParseError error) {
      synchronize();
      return null;
    }
  }
// make list of expressions to be printed and identified whishch statement id calling (print or puts)
  private Stmt printStatement(TokenType token) {
    List<Expr> value = expressionList();
    consume(NEWLINE, "Expect newline after value.");
    boolean type = (token == PUTS) ? true : false;
    return new Stmt.Print(value, type);
  }
  private Stmt returnStatement() {
    Token keyword = previous();
    Expr value = null;
    //if (!check(SEMICOLON)) {
      value = expression();
    //}
    //consume(SEMICOLON, "Expect ';' after return value.");
    return new Stmt.Return(keyword, value);
  }
  // private Stmt putsStatement() {
  // List<Expr> value = expressionList();
  // consume(NEWLINE, "Expect newline after value.");
  // return new Stmt.Print(value, PUTS);
  // }

  private Stmt expressionStatement() {
    Expr expr = expression();
    consume(NEWLINE, "Expect newline after value.");
    return new Stmt.Expression(expr);
  }

  /*private Stmt.Function function(String kind) {
    List<Token> parameters = new ArrayList<>();
    Token name = consume(IDENTIFIER, "Expect " + kind + " name.");
    consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");
      //List<Token> parameters = new ArrayList<>();
      if (!check(RIGHT_PAREN)) {
        do {
          if (parameters.size() >= 255) {
            error(peek(), "Can't have more than 255 parameters.");
          }
          parameters.add(
              consume(IDENTIFIER, "Expect parameter name."));
        } while (match(COMMA));
      }
      consume(RIGHT_PAREN, "Expect ')' after parameters.");
      //consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
      List<Stmt> body = block();
      return new Stmt.Function(name, parameters, body);
  }
  */
  private Stmt.Function function(String kind) {
    List<Token> parameters = new ArrayList<>();
    Token name = consume(IDENTIFIER, "Expect " + kind + " name.");
  
    // Check for optional left parenthesis '(' after the function name.
    if (match(LEFT_PAREN)) {
      // Parse parameters only if there is a left parenthesis.
      if (!check(RIGHT_PAREN)) {
        do {
          if (parameters.size() >= 255) {
            error(peek(), "Can't have more than 255 parameters.");
          }
          parameters.add(
              consume(IDENTIFIER, "Expect parameter name."));
        } while (match(COMMA));
      }
      consume(RIGHT_PAREN, "Expect ')' after parameters.");
    }
  
    // Parse the function body using the block function (not provided).
    List<Stmt> body = block();
  
    return new Stmt.Function(name, parameters, body);
  }
  
  private List<Stmt> block() {
    List<Stmt> statements = new ArrayList<>();

    while (!check(END) && !isAtEnd()) {
      statements.add(declaration());
    }

    consume(END, "Expect 'end' after block.");
    return statements;
  }
//parsing and and or logical operators
  private Expr and() {
    Expr expr = equality();

    while (match(AND)) {
      Token operator = previous();
      Expr right = equality();
      expr = new Expr.Logical(expr, operator, right);
    }

    return expr;
  }

  private Expr or() {
    Expr expr = and();

    while (match(OR)) {
      Token operator = previous();
      Expr right = and();
      expr = new Expr.Logical(expr, operator, right);
    }

    return expr;
  }
// parsing assignment operators
  private Expr assignment() {
    Expr expr = or();

    while (match(EQUAL, PLUS_EQUAL, MINUS_EQUAL, STAR_EQUAL, SLASH_EQUAL, MOD_EQUAL)) {
      Token operator = previous();
      Expr value = assignment();

      if (expr instanceof Expr.Variable) {
        Token name = ((Expr.Variable) expr).name;
        expr = new Expr.Assign(name, operator, value);
      }

      // error(operator, "Invalid assignment target.");
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

  private Token superPeek() {
    if (!isAtEnd()) {
      return tokens.get(current + 1);
    }
    return null;
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