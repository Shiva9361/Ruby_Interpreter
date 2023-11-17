package ruby;

//An enum type is a special data type that enables for a variable to be a set of predefined constants.
/*
 * We added a large number of tokens, Out of which we tried to implement most 
 */
enum TokenType {

    // Single char tokens
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, LEFT_SQUARE, RIGHT_SQUARE,
    COMMA, SEMICOLON, COLON, COLON_COLON, QUESTION_MARK,
    BACKSLASH, ARROW_OP, FAT_ARROW_OP, DOT,
    ELLIPSES, // ahhh should check // argumnet forwarding havent't added token for this
    // general arithmetic ops
    MINUS, PLUS, SLASH, STAR, MOD,
    // One/two char tokens
    BANG_EQUAL, EQUAL_EQUAL, GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,
    // Logical ops && ||
    AMPERSAND_AMPERSAND, PIPE_PIPE, BANG,
    // Assignment ops
    EQUAL, PLUS_EQUAL, MINUS_EQUAL, STAR_EQUAL, SLASH_EQUAL, MOD_EQUAL, STAR_STAR,
    // Bitwise ops
    AMPERSAND, PIPE, KARROT, TILDA, LEFT_SHIFT, RIGHT_SHIFT,
    // literals
    IDENTIFIER, GLOBAL_IDENTIFIER, CONSTANT_IDENTIFIER, INTEGER, FLOAT, STRING, ARRAY, HASH, REGEX,
    // Keywords
    IF, ELSE, ELSIF, UNLESS, WHILE, FOR, LOOP, DO, END, DEF, CLASS, MODULE,
    RETURN, BREAK, NEXT, NIL, TRUE, FALSE, SUPER, SELF, CASE, WHEN, BEGIN,
    RESCUE, ENSURE, RETRY, NOT, AND, OR, IN, THEN, UNTIL, REDO,

    NEWLINE, // We need newline in ruby parsing
    PRINT, PUTS,
    BEGIN_C, END_C, // Both capital and small begin
    DOT_DOT_DOT, DOT_DOT, // For forLoop 
    EOF

}
/*
 * The main token class 
 */
public class Token {
    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line;
    /*
     * Paramaterized constructor for Token, stores the type from enum
     * lexeme, the literal object and the line number
     */
    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }
    /*
     * Method to print Token for debugging purposes 
     * returns the needed data as a string
     */
    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}